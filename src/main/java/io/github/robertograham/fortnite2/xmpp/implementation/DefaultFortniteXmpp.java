package io.github.robertograham.fortnite2.xmpp.implementation;

import io.github.robertograham.fortnite2.client.Fortnite;
import io.github.robertograham.fortnite2.xmpp.client.FortniteXmpp;
import io.github.robertograham.fortnite2.xmpp.domain.Session;
import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Application;
import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Platform;
import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Status;
import io.github.robertograham.fortnite2.xmpp.listener.OnChatMessageReceivedListener;
import io.github.robertograham.fortnite2.xmpp.listener.OnFriendPresenceReceivedListener;
import io.github.robertograham.fortnite2.xmpp.listener.OnFriendsListReceivedListener;
import io.github.robertograham.fortnite2.xmpp.resource.ChatResource;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link FortniteXmpp} created using {@link DefaultFortniteXmpp.Builder}
 */
public final class DefaultFortniteXmpp implements FortniteXmpp {

    private final Domainpart xmppDomainpart;
    private final Fortnite fortnite;
    private final XMPPTCPConnection prodServiceXmppTcpConnection;
    private final StanzaListener rosterPacketListener;
    private final StanzaListener presenceListener;
    private final OnFriendsListReceivedListener onFriendsListReceivedListener;
    private final OnFriendPresenceReceivedListener onFriendPresenceReceivedListener;
    private final DefaultChatResource chatResource;
    private final PingManager pingManager;

    private DefaultFortniteXmpp(final Builder builder) throws InterruptedException, XMPPException, SmackException, IOException {
        fortnite = builder.fortnite;
        onFriendsListReceivedListener = builder.onFriendsListReceivedListener;
        onFriendPresenceReceivedListener = builder.onFriendPresenceReceivedListener;
        SmackConfiguration.DEBUG = builder.debugXmppConnections;
        xmppDomainpart = Domainpart.fromOrThrowUnchecked("prod.ol.epicgames.com");
        prodServiceXmppTcpConnection = buildProdServiceXmppTcpConnection();
        rosterPacketListener = createRosterPacketListener();
        presenceListener = createPresenceListener();
        prodServiceXmppTcpConnection.addAsyncStanzaListener(rosterPacketListener, new StanzaTypeFilter(RosterPacket.class));
        prodServiceXmppTcpConnection.addAsyncStanzaListener(presenceListener, new StanzaTypeFilter(Presence.class));
        chatResource = DefaultChatResource.newInstance(builder.onChatMessageReceivedListener, prodServiceXmppTcpConnection);
        prodServiceXmppTcpConnection.connect()
            .login(
                fortnite.session().accountId(),
                fortnite.session().accessToken(),
                Resourcepart.from(String.format(
                    "V2:%s:%s",
                    builder.application.code(),
                    builder.platform.code()
                ))
            );
        pingManager = PingManager.getInstanceFor(prodServiceXmppTcpConnection);
        pingManager.setPingInterval(Math.toIntExact(TimeUnit.MINUTES.toSeconds(4) + 30L));
    }

    private XMPPTCPConnection buildProdServiceXmppTcpConnection() {
        return new XMPPTCPConnection(XMPPTCPConnectionConfiguration.builder()
            .setXmppDomain(JidCreate.domainBareFrom(xmppDomainpart))
            .setHost("xmpp-service-prod.ol.epicgames.com")
            .setPort(5222)
            .build());
    }

    private StanzaListener createRosterPacketListener() {
        return (final var stanza) -> Optional.of(stanza)
            .filter((final var nonNullStanza) -> nonNullStanza instanceof RosterPacket)
            .map((final var nonNullStanza) -> (RosterPacket) nonNullStanza)
            .map((final var rosterPacket) -> rosterPacket.getRosterItems().stream()
                .map(RosterPacket.Item::getJid)
                .map(Jid::getLocalpartOrNull)
                .filter(Objects::nonNull)
                .map(Localpart::asUnescapedString)
                .collect(Collectors.toSet()))
            .ifPresent(onFriendsListReceivedListener::onFriendsListReceived);
    }

    private StanzaListener createPresenceListener() {
        return (final var stanza) -> Optional.of(stanza)
            .filter((final var nonNullStanza) -> nonNullStanza instanceof Presence)
            .map((final var nonNullStanza) -> (Presence) nonNullStanza)
            .filter((final var presence) -> presence.isAvailable()
                || Presence.Type.unavailable == presence.getType())
            .ifPresent((final var presence) -> Optional.ofNullable(presence.getFrom().getLocalpartOrNull())
                .map(Localpart::asUnescapedString)
                .filter((final var accountId) -> !fortnite.session().accountId().equals(accountId))
                .ifPresent((final var accountId) -> {
                    final var status = presence.isAvailable() ?
                        presence.isAway() ?
                            Status.AWAY
                            : Status.ONLINE
                        : Status.OFFLINE;
                    final var sessionOptional = JsonToOptionalOfClassParser.INSTANCE.parseJsonToOptionalOfClass(DefaultSession.class, presence.getStatus())
                        .map((final var defaultSession) -> (Session) defaultSession);
                    onFriendPresenceReceivedListener.onFriendPresenceReceived(accountId, status, sessionOptional);
                }));
    }

    @Override
    public void close() {
        chatResource.close();
        prodServiceXmppTcpConnection.removeAsyncStanzaListener(rosterPacketListener);
        prodServiceXmppTcpConnection.removeAsyncStanzaListener(presenceListener);
        pingManager.setPingInterval(-1);
        prodServiceXmppTcpConnection.disconnect();
    }

    @Override
    public ChatResource chat() {
        return chatResource;
    }

    @Override
    public XMPPTCPConnection prodService() {
        return prodServiceXmppTcpConnection;
    }

    /**
     * Used to create {@link FortniteXmpp} instances
     * Instantiated using {@link #newInstance(Fortnite)}
     */
    public static final class Builder {

        private final Fortnite fortnite;
        private OnChatMessageReceivedListener onChatMessageReceivedListener = (final var accountId, final var messageBody) -> {
        };
        private OnFriendsListReceivedListener onFriendsListReceivedListener = (final var accountIds) -> {
        };
        private OnFriendPresenceReceivedListener onFriendPresenceReceivedListener = (final var accountId, final var status, final var sessionOptional) -> {
        };
        private boolean debugXmppConnections = false;
        private Application application = Application.FORTNITE_CLIENT;
        private Platform platform = Platform.WINDOWS;

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
         * @param onFriendsListReceivedListener the {@link OnFriendsListReceivedListener} to call
         *                                      {@link OnFriendsListReceivedListener#onFriendsListReceived(Set)}
         *                                      on when the authenticated user's friends list is received after log in
         * @return the {@link Builder} instance this was called on
         * @throws NullPointerException if {@code onFriendsListReceivedListener} is {@code null}
         */
        public Builder setOnFriendsListReceivedListener(final OnFriendsListReceivedListener onFriendsListReceivedListener) {
            this.onFriendsListReceivedListener = Objects.requireNonNull(onFriendsListReceivedListener, "onFriendsListReceivedListener cannot be null");
            return this;
        }

        /**
         * @param onFriendPresenceReceivedListener the {@link OnFriendPresenceReceivedListener} to call
         *                                         {@link OnFriendPresenceReceivedListener#onFriendPresenceReceived(String, Status, Optional)}
         *                                         on a friend's presence is received
         * @return the {@link Builder} instance this was called on
         * @throws NullPointerException if {@code onFriendPresenceReceivedListener} is {@code null}
         */
        public Builder setOnFriendPresenceReceivedListener(final OnFriendPresenceReceivedListener onFriendPresenceReceivedListener) {
            this.onFriendPresenceReceivedListener = Objects.requireNonNull(onFriendPresenceReceivedListener, "onFriendPresenceReceivedListener cannot be null");
            return this;
        }

        /**
         * @param debugXmppConnections whether or not to log XMPP connection traffic
         *                             {@code true} to log and {@code} to not log
         * @return the {@link Builder} instance this was called on
         */
        public Builder setDebugXmppConnections(final boolean debugXmppConnections) {
            this.debugXmppConnections = debugXmppConnections;
            return this;
        }

        /**
         * @param application depending on the value, the authenticated user will appear to
         *                    either be playing Fortnite or in the EpicGames Launcher to friends
         * @return the {@link Builder} instance this was called on
         * @throws NullPointerException if {@code application} is {@code null}
         */
        public Builder setApplication(final Application application) {
            this.application = Objects.requireNonNull(application, "application cannot be null");
            return this;
        }

        /**
         * @param platform the icon representing this will be shown to friends of the
         *                 authenticated user
         * @return the {@link Builder} instance this was called on
         * @throws NullPointerException if {@code platform} is {@code null}
         */
        public Builder setPlatform(final Platform platform) {
            this.platform = Objects.requireNonNull(platform, "platform cannot be null");
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
