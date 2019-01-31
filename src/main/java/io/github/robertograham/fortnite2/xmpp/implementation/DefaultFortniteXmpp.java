package io.github.robertograham.fortnite2.xmpp.implementation;

import io.github.robertograham.fortnite2.client.Fortnite;
import io.github.robertograham.fortnite2.xmpp.client.FortniteXmpp;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;

import java.io.IOException;
import java.util.Objects;

public final class DefaultFortniteXmpp implements FortniteXmpp {

    private final Domainpart xmppDomainpart;
    private final Fortnite fortnite;
    private final XMPPTCPConnection prodServiceXmppTcpConnection;

    private DefaultFortniteXmpp(final Builder builder) throws InterruptedException, XMPPException, SmackException, IOException {
        fortnite = builder.fortnite;
        SmackConfiguration.DEBUG = builder.debugXmppConnections;
        xmppDomainpart = Domainpart.fromOrThrowUnchecked("prod.ol.epicgames.com");
        prodServiceXmppTcpConnection = buildProdServiceXmppTcpConnection();
        prodServiceXmppTcpConnection.connect()
            .login(fortnite.session().accountId(), fortnite.session().accessToken());
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

    public static final class Builder {

        private final Fortnite fortnite;
        private boolean debugXmppConnections = false;

        private Builder(final Fortnite fortnite) {
            this.fortnite = fortnite;
        }

        public static Builder newInstance(final Fortnite fortnite) {
            Objects.requireNonNull(fortnite, "fortnite cannot be null");
            return new Builder(fortnite);
        }

        public Builder setDebugXmppConnections(boolean debugXmppConnections) {
            this.debugXmppConnections = debugXmppConnections;
            return this;
        }

        public FortniteXmpp build() {
            try {
                return new DefaultFortniteXmpp(this);
            } catch (final InterruptedException | XMPPException | SmackException | IOException exception) {
                throw new IllegalStateException("Error occurred when establishing an XMPP connection", exception);
            }
        }
    }
}
