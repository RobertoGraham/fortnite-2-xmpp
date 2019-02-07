package io.github.robertograham.fortnite2.xmpp.implementation;

import io.github.robertograham.fortnite2.xmpp.client.FortniteXmpp;
import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Status;
import io.github.robertograham.fortnite2.xmpp.listener.OnChatMessageReceivedListener;
import io.github.robertograham.fortnite2.xmpp.resource.ChatResource;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

final class DefaultChatResource implements ChatResource, AutoCloseable, IncomingChatMessageListener, OutgoingChatMessageListener {

    private static final Set<Status> AVAILABLE_PRESENCE_STATUSES = Set.of(Status.ONLINE, Status.AWAY);
    private final OnChatMessageReceivedListener onChatMessageReceivedListener;
    private final XMPPTCPConnection prodServiceXmppTcpConnection;
    private final FortniteXmpp fortniteXmpp;
    private final Map<String, List<Message>> incomingChatMessageCacheMap;
    private final Map<String, List<Message>> outgoingChatMessageCacheMap;
    private final ChatManager chatManager;

    private DefaultChatResource(final OnChatMessageReceivedListener onChatMessageReceivedListener,
                                final XMPPTCPConnection prodServiceXmppTcpConnection,
                                final FortniteXmpp fortniteXmpp) {
        this.onChatMessageReceivedListener = onChatMessageReceivedListener;
        this.prodServiceXmppTcpConnection = prodServiceXmppTcpConnection;
        this.fortniteXmpp = fortniteXmpp;
        incomingChatMessageCacheMap = new HashMap<>();
        outgoingChatMessageCacheMap = new HashMap<>();
        chatManager = ChatManager.getInstanceFor(this.prodServiceXmppTcpConnection);
        chatManager.addIncomingListener(this);
        chatManager.addOutgoingListener(this);
    }

    static DefaultChatResource newInstance(final OnChatMessageReceivedListener onChatMessageReceivedListener,
                                           final XMPPTCPConnection prodServiceXmppTcpConnection,
                                           final FortniteXmpp fortniteXmpp) {
        return new DefaultChatResource(
            onChatMessageReceivedListener,
            prodServiceXmppTcpConnection,
            fortniteXmpp
        );
    }

    @Override
    public void sendMessageToAccountId(final String accountId, final String messageBody) throws IOException {
        Objects.requireNonNull(accountId, "accountId cannot be null");
        Objects.requireNonNull(messageBody, "messageBody cannot be null");
        try {
            chatManager.chatWith(JidCreate.entityBareFrom(
                Localpart.fromOrThrowUnchecked(accountId),
                prodServiceXmppTcpConnection.getXMPPServiceDomain()
            ))
                .send(messageBody);
        } catch (final SmackException.NotConnectedException | InterruptedException exception) {
            throw new IOException("Failed to send message", exception);
        }
    }

    @Override
    public void updateStatus(final Status status) throws IOException {
        Objects.requireNonNull(status, "status cannot be null");
        final var presence = createPresence(status);
        try {
            prodServiceXmppTcpConnection.sendStanza(presence);
        } catch (final SmackException.NotConnectedException | InterruptedException exception) {
            throw new IOException("Failed to update status", exception);
        }
    }

    @Override
    public List<String> findAllMessagesSentToAccountId(final String accountId) {
        Objects.requireNonNull(accountId, "accountId cannot be null");
        return getListOfChatMessageBodiesCachedForAccountId(outgoingChatMessageCacheMap, accountId);
    }

    @Override
    public List<String> findAllMessagesReceivedFromAccountId(final String accountId) {
        Objects.requireNonNull(accountId, "accountId cannot be null");
        return getListOfChatMessageBodiesCachedForAccountId(incomingChatMessageCacheMap, accountId);
    }

    @Override
    public FortniteXmpp fortniteXmpp() {
        return fortniteXmpp;
    }

    private List<String> getListOfChatMessageBodiesCachedForAccountId(final Map<String, List<Message>> chatMessageCacheMap,
                                                                      final String accountId) {
        return Optional.ofNullable(chatMessageCacheMap.get(accountId))
            .map((final var messageList) -> messageList.stream()
                .map(Message::getBody)
                .collect(Collectors.toUnmodifiableList()))
            .orElseGet(Collections::emptyList);
    }

    private Presence createPresence(final Status status) {
        final var presenceType = AVAILABLE_PRESENCE_STATUSES.contains(status) ?
            Presence.Type.available
            : Presence.Type.unavailable;
        final var presence = new Presence(presenceType);
        if (Status.AWAY == status)
            presence.setMode(Presence.Mode.away);
        return presence;
    }

    @Override
    public void close() {
        chatManager.removeIncomingListener(this);
        chatManager.removeOutgoingListener(this);
    }

    @Override
    public void newIncomingMessage(final EntityBareJid from, final Message message, final Chat chat) {
        if (Message.Type.chat == message.getType()) {
            final var accountIdString = getAccountIdFromEntityBareJid(from);
            cacheChatMessage(incomingChatMessageCacheMap, accountIdString, message);
            onChatMessageReceivedListener.onChatMessageReceived(accountIdString, message.getBody(), this);
        }
    }

    @Override
    public void newOutgoingMessage(final EntityBareJid to, final Message message, final Chat chat) {
        if (Message.Type.chat == message.getType())
            cacheChatMessage(outgoingChatMessageCacheMap, getAccountIdFromEntityBareJid(to), message);
    }

    private String getAccountIdFromEntityBareJid(final EntityBareJid entityBareJid) {
        return entityBareJid.getLocalpart()
            .asUnescapedString();
    }

    private void cacheChatMessage(final Map<String, List<Message>> chatMessageCacheMap,
                                  final String accountId,
                                  final Message message) {
        chatMessageCacheMap.putIfAbsent(accountId, new ArrayList<>());
        chatMessageCacheMap.get(accountId)
            .add(message);
    }
}
