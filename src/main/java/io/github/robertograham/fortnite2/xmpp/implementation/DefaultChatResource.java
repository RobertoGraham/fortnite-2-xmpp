package io.github.robertograham.fortnite2.xmpp.implementation;

import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Status;
import io.github.robertograham.fortnite2.xmpp.listener.OnChatMessageReceivedListener;
import io.github.robertograham.fortnite2.xmpp.resource.ChatResource;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

final class DefaultChatResource implements ChatResource, AutoCloseable {

    private static final Set<Status> AVAILABLE_PRESENCE_STATUSES = Set.of(Status.ONLINE, Status.AWAY);
    private final ChatManager chatManager;
    private final OnChatMessageReceivedListener onChatMessageReceivedListener;
    private final XMPPTCPConnection prodServiceXmppTcpConnection;
    private final IncomingChatMessageListener incomingChatMessageListener;

    private DefaultChatResource(final OnChatMessageReceivedListener onChatMessageReceivedListener,
                                final XMPPTCPConnection prodServiceXmppTcpConnection) {
        this.onChatMessageReceivedListener = onChatMessageReceivedListener;
        this.prodServiceXmppTcpConnection = prodServiceXmppTcpConnection;
        chatManager = ChatManager.getInstanceFor(this.prodServiceXmppTcpConnection);
        incomingChatMessageListener = createIncomingChatMessageListener();
        chatManager.addIncomingListener(incomingChatMessageListener);
    }

    static DefaultChatResource newInstance(final OnChatMessageReceivedListener onChatMessageReceivedListener,
                                           final XMPPTCPConnection prodServiceXmppTcpConnection) {
        return new DefaultChatResource(
            onChatMessageReceivedListener,
            prodServiceXmppTcpConnection
        );
    }

    private IncomingChatMessageListener createIncomingChatMessageListener() {
        return (entityBareJid, message, chat) -> {
            if (Message.Type.chat == message.getType())
                onChatMessageReceivedListener.onChatMessageReceived(
                    entityBareJid.getLocalpart().asUnescapedString(),
                    message.getBody()
                );
        };
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
        chatManager.removeIncomingListener(incomingChatMessageListener);
    }
}
