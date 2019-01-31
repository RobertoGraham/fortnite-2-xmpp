package io.github.robertograham.fortnite2.xmpp.implementation;

import io.github.robertograham.fortnite2.client.Fortnite;
import io.github.robertograham.fortnite2.xmpp.client.FortniteXmpp;
import io.github.robertograham.fortnite2.xmpp.listener.OnChatMessageReceivedListener;
import io.github.robertograham.fortnite2.xmpp.resource.ChatResource;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;

import java.io.IOException;
import java.util.Objects;

/**
 * Default implementation of {@link FortniteXmpp} created using {@link DefaultFortniteXmpp.Builder}
 */
public final class DefaultFortniteXmpp implements FortniteXmpp {

    private final Domainpart xmppDomainpart;
    private final Fortnite fortnite;
    private final XMPPTCPConnection prodServiceXmppTcpConnection;
    private final OnChatMessageReceivedListener onChatMessageReceivedListener;
    private final ChatResource chatResource;

    private DefaultFortniteXmpp(final Builder builder) throws InterruptedException, XMPPException, SmackException, IOException {
        fortnite = builder.fortnite;
        onChatMessageReceivedListener = builder.onChatMessageReceivedListener;
        SmackConfiguration.DEBUG = builder.debugXmppConnections;
        xmppDomainpart = Domainpart.fromOrThrowUnchecked("prod.ol.epicgames.com");
        prodServiceXmppTcpConnection = buildProdServiceXmppTcpConnection();
        prodServiceXmppTcpConnection.connect()
            .login(fortnite.session().accountId(), fortnite.session().accessToken());
        chatResource = DefaultChatResource.newInstance(onChatMessageReceivedListener, prodServiceXmppTcpConnection);
    }

    private XMPPTCPConnection buildProdServiceXmppTcpConnection() {
        return new XMPPTCPConnection(XMPPTCPConnectionConfiguration.builder()
            .setXmppDomain(JidCreate.domainBareFrom(xmppDomainpart))
            .setHost("xmpp-service-prod.ol.epicgames.com")
            .setPort(5222)
            .build());
    }

    @Override
    public void close() {
        prodServiceXmppTcpConnection.disconnect();
    }

    @Override
    public ChatResource chat() {
        return chatResource;
    }

    /**
     * Used to create {@link FortniteXmpp} instances
     * Instantiated using {@link #newInstance(Fortnite)}
     */
    public static final class Builder {

        private final Fortnite fortnite;
        private OnChatMessageReceivedListener onChatMessageReceivedListener = (final var accountId, final var messageBody) -> {
        };
        private boolean debugXmppConnections = false;

        private Builder(final Fortnite fortnite) {
            this.fortnite = fortnite;
        }

        /**
         * @param fortnite the {@link Fortnite} instance to get authentication session details from
         * @return a new {@link Builder} instance
         * @throws NullPointerException if {@code fortnite} is {@code null}
         */
        public static Builder newInstance(final Fortnite fortnite) {
            Objects.requireNonNull(fortnite, "fortnite cannot be null");
            return new Builder(fortnite);
        }

        /**
         * @param onChatMessageReceivedListener the {@link OnChatMessageReceivedListener} to call
         *                                      {@link OnChatMessageReceivedListener#onChatMessageReceived(String, String)}
         *                                      on when an incoming chat message is intercepted
         * @return the {@link Builder} instance this was called on
         * @throws NullPointerException if {@code onChatMessageReceivedListener} is {@code null}
         */
        public Builder setOnChatMessageReceivedListener(final OnChatMessageReceivedListener onChatMessageReceivedListener) {
            this.onChatMessageReceivedListener = Objects.requireNonNull(onChatMessageReceivedListener, "onChatMessageReceivedListener cannot be null");
            return this;
        }

        /**
         * @param debugXmppConnections whether or not to log XMPP connection traffic
         *                             {@code true} to log and {@code} to not log
         * @return the {@link Builder} instance this was called on
         */
        public Builder setDebugXmppConnections(boolean debugXmppConnections) {
            this.debugXmppConnections = debugXmppConnections;
            return this;
        }

        /**
         * @return a new instance of {@link FortniteXmpp}
         * @throws IllegalStateException if there's a problem establishing any XMPP connections
         */
        public FortniteXmpp build() {
            try {
                return new DefaultFortniteXmpp(this);
            } catch (final InterruptedException | XMPPException | SmackException | IOException exception) {
                throw new IllegalStateException("Error occurred when establishing an XMPP connection", exception);
            }
        }
    }
}
