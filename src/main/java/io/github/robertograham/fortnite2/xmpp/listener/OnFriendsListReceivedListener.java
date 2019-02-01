package io.github.robertograham.fortnite2.xmpp.listener;

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
     * @since 1.1.0
     */
    void onFriendsListReceived(final Set<String> accountIds);
}
