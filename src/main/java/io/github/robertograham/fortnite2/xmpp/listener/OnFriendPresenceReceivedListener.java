package io.github.robertograham.fortnite2.xmpp.listener;

import io.github.robertograham.fortnite2.xmpp.domain.Session;
import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Status;
import io.github.robertograham.fortnite2.xmpp.resource.FriendResource;

import java.util.Optional;

/**
 * Registered using {@link io.github.robertograham.fortnite2.xmpp.implementation.DefaultFortniteXmpp.Builder#setOnFriendPresenceReceivedListener(OnFriendPresenceReceivedListener)}
 *
 * @since 1.1.0
 */
@FunctionalInterface
public interface OnFriendPresenceReceivedListener {

    /**
     * Called when a friend's presence is received
     *
     * @param accountId       the ID of the friend's account
     * @param status          {@link Status#ONLINE} if the friend is online and available
     *                        {@link Status#AWAY} if the friend is online and unavailable
     *                        {@link Status#OFFLINE}  if the friend is offline
     * @param sessionOptional an {@link Optional} of {@link Session}
     *                        that's non-empty if {@code status} is not equal to {@link Status#OFFLINE}
     * @param friend          {@link FriendResource} instance that can be used perform actions with the
     *                        {@link Session} instance
     * @since 2.0.0
     */
    void onFriendPresenceReceived(final String accountId,
                                  final Status status,
                                  final Optional<Session> sessionOptional,
                                  final FriendResource friend);
}
