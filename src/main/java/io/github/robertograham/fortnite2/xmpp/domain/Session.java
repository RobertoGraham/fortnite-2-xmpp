package io.github.robertograham.fortnite2.xmpp.domain;

/**
 * Contains information about a friend's session
 *
 * @since 1.1.0
 */
public interface Session {

    /**
     * @return The session ID of the match that a friend is in.
     * Empty if the friend is not in a match
     * @since 1.1.0
     */
    String id();

    /**
     * @return Details the population of a friend's party if they aren't in a match.
     * Details the population of a friend's match if they are in a match
     * @since 1.1.0
     */
    String status();

    /**
     * @return {@code true} if the friend is in a match, {@code false} otherwise
     * @since 1.1.0
     */
    boolean isPlaying();

    /**
     * @return {@code true} if the friend's party/match can be joined, {@code false} otherwise
     * @since 1.1.0
     */
    boolean isJoinable();

    /**
     * @return {@code true} if the friend's party/match has voice support, {@code false} otherwise
     * @since 1.1.0
     */
    boolean hasVoiceSupport();
}
