package io.github.robertograham.fortnite2.xmpp.implementation;

import io.github.robertograham.fortnite2.xmpp.listener.OnChatMessageReceivedListener;
import io.github.robertograham.fortnite2.xmpp.resource.ChatResource;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

import java.io.IOException;
import java.util.Objects;

final class DefaultChatResource implements ChatResource {

    private final ChatManager chatManager;
    private final OnChatMessageReceivedListener onChatMessageReceivedListener;
    private final XMPPTCPConnection prodServiceXmppTcpConnection;

    private DefaultChatResource(final OnChatMessageReceivedListener onChatMessageReceivedListener,
                                final XMPPTCPConnection prodServiceXmppTcpConnection) {
        this.onChatMessageReceivedListener = onChatMessageReceivedListener;
        this.prodServiceXmppTcpConnection = prodServiceXmppTcpConnection;
        chatManager = ChatManager.getInstanceFor(this.prodServiceXmppTcpConnection);
        chatManager.addIncomingListener((entityBareJid, message, chat) ->
            this.onChatMessageReceivedListener.onChatMessageReceived(
                entityBareJid.getLocalpart().asUnescapedString(),
                message.getBody()
            )
        );
    }

    static DefaultChatResource newInstance(final OnChatMessageReceivedListener onChatMessageReceivedListener,
                                           final XMPPTCPConnection prodServiceXmppTcpConnection) {
        return new DefaultChatResource(
            onChatMessageReceivedListener,
            prodServiceXmppTcpConnection
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
}
