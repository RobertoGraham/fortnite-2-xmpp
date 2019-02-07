package io.github.robertograham.fortnite2.xmpp.implementation;

import io.github.robertograham.fortnite2.xmpp.client.FortniteXmpp;
import io.github.robertograham.fortnite2.xmpp.domain.Session;
import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Status;
import io.github.robertograham.fortnite2.xmpp.listener.OnFriendPresenceReceivedListener;
import io.github.robertograham.fortnite2.xmpp.listener.OnFriendsListReceivedListener;
import io.github.robertograham.fortnite2.xmpp.resource.FriendResource;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.AbstractRosterListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

import javax.json.bind.JsonbException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class DefaultFriendResource implements FriendResource, AutoCloseable {

    private final XMPPTCPConnection prodServiceXmppTcpConnection;
    private final FortniteXmpp fortniteXmpp;
    private final Roster roster;
    private final RosterListener rosterListener;

    private DefaultFriendResource(final XMPPTCPConnection prodServiceXmppTcpConnection,
                                  final OnFriendsListReceivedListener onFriendsListReceivedListener,
                                  final OnFriendPresenceReceivedListener onFriendPresenceReceivedListener,
                                  final FortniteXmpp fortniteXmpp) {
        this.prodServiceXmppTcpConnection = prodServiceXmppTcpConnection;
        this.fortniteXmpp = fortniteXmpp;
        roster = Roster.getInstanceFor(this.prodServiceXmppTcpConnection);
        rosterListener = new DefaultRosterListener(
            roster::getEntries,
            roster::getPresence,
            prodServiceXmppTcpConnection.getXMPPServiceDomain(),
            onFriendsListReceivedListener,
            onFriendPresenceReceivedListener,
            this
        );
        roster.addRosterListener(rosterListener);
    }

    static DefaultFriendResource newInstance(final XMPPTCPConnection prodServiceXmppTcpConnection,
                                             final OnFriendsListReceivedListener onFriendsListReceivedListener,
                                             final OnFriendPresenceReceivedListener onFriendPresenceReceivedListener,
                                             final FortniteXmpp fortniteXmpp) {
        return new DefaultFriendResource(
            prodServiceXmppTcpConnection,
            onFriendsListReceivedListener,
            onFriendPresenceReceivedListener,
            fortniteXmpp
        );
    }

    @Override
    public void close() {
        roster.removeRosterListener(rosterListener);
    }

    @Override
    public FortniteXmpp fortniteXmpp() {
        return fortniteXmpp;
    }

    private final static class DefaultRosterListener extends AbstractRosterListener {

        private final Supplier<Set<RosterEntry>> rosterEntrySetSupplier;
        private final Function<BareJid, Presence> bareJidToPresenceFunction;
        private final DomainBareJid domainBareJid;
        private final OnFriendsListReceivedListener onFriendsListReceivedListener;
        private final OnFriendPresenceReceivedListener onFriendPresenceReceivedListener;
        private final FriendResource friendResource;

        private DefaultRosterListener(final Supplier<Set<RosterEntry>> rosterEntrySetSupplier,
                                      final Function<BareJid, Presence> bareJidToPresenceFunction,
                                      final DomainBareJid domainBareJid,
                                      final OnFriendsListReceivedListener onFriendsListReceivedListener,
                                      final OnFriendPresenceReceivedListener onFriendPresenceReceivedListener,
                                      final FriendResource friendResource) {
            this.rosterEntrySetSupplier = rosterEntrySetSupplier;
            this.bareJidToPresenceFunction = bareJidToPresenceFunction;
            this.domainBareJid = domainBareJid;
            this.onFriendsListReceivedListener = onFriendsListReceivedListener;
            this.onFriendPresenceReceivedListener = onFriendPresenceReceivedListener;
            this.friendResource = friendResource;
        }

        @Override
        public void entriesUpdated(final Collection<Jid> addresses) {
            updateOnFriendsListReceivedListener();
        }

        @Override
        public void entriesAdded(final Collection<Jid> addresses) {
            updateOnFriendsListReceivedListener();
        }

        @Override
        public void entriesDeleted(final Collection<Jid> addresses) {
            updateOnFriendsListReceivedListener();
        }

        private void updateOnFriendsListReceivedListener() {
            onFriendsListReceivedListener.onFriendsListReceived(
                rosterEntrySetSupplier.get().stream()
                    .map(RosterEntry::getJid)
                    .map(BareJid::getLocalpartOrNull)
                    .filter(Objects::nonNull)
                    .map(Localpart::asUnescapedString)
                    .collect(Collectors.toUnmodifiableSet()),
                friendResource
            );
        }

        @Override
        public void presenceChanged(final Presence presence) {
            Optional.ofNullable(presence.getFrom().getLocalpartOrNull())
                .ifPresent((final var localpart) -> {
                    final var bestPresence = bareJidToPresenceFunction.apply(JidCreate.bareFrom(
                        localpart,
                        domainBareJid
                    ));
                    final var status = bestPresence.isAvailable() ?
                        bestPresence.isAway() ?
                            Status.AWAY
                            : Status.ONLINE
                        : Status.OFFLINE;
                    var sessionOptional = Optional.<Session>empty();
                    try {
                        sessionOptional = JsonToOptionalOfClassParser.INSTANCE.parseJsonToOptionalOfClass(DefaultSession.class, bestPresence.getStatus())
                            .map(Function.identity());
                    } catch (final JsonbException exception) {
                        exception.printStackTrace();
                    }
                    onFriendPresenceReceivedListener.onFriendPresenceReceived(
                        localpart.asUnescapedString(),
                        status,
                        sessionOptional,
                        friendResource
                    );
                });
        }
    }
}
