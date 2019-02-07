package io.github.robertograham.fortnite2.xmpp.resource;

import io.github.robertograham.fortnite2.xmpp.client.FortniteXmpp;

/**
 * an object from which friend-related actions can be performed
 *
 * @since 2.0.0
 */
public interface FriendResource {

    /**
     * @return the instance of {@link FortniteXmpp} this object belongs to
     * @since 2.0.0
     */
    FortniteXmpp fortniteXmpp();
}
