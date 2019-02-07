package io.github.robertograham.fortnite2.xmpp.listener;

import io.github.robertograham.fortnite2.xmpp.resource.FriendResource;

import java.util.Set;

/**
 * Registered using {@link io.github.robertograham.fortnite2.xmpp.implementation.DefaultFortniteXmpp.Builder#setOnFriendsListReceivedListener(OnFriendsListReceivedListener)}
 *
 * @since 1.1.0
 */
@FunctionalInterface
public interface OnFriendsListReceivedListener {

    /**
     * Called after login when friends list is received
     *
     * @param accountIds IDs of the accounts that the authenticated user is friends with
     * @param friend     instance that can be used perform actions with the account IDs
     * @since 2.0.0
     */
    void onFriendsListReceived(final Set<String> accountIds, final FriendResource friend);
}
