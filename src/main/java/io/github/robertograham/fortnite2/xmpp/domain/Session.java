package io.github.robertograham.fortnite2.xmpp.domain;

import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Application;
import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Platform;

import java.util.Optional;

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

    /**
     * @return an {@link Optional} of {@link Integer} that's non-empty if the user is in a party
     * @since 1.3.0
     */
    Optional<Integer> partyMemberCount();

    /**
     * @return an {@link Optional} of {@link Integer} that's non-empty if the user is in a party
     * @since 1.3.0
     */
    Optional<Integer> maxPartyMemberCount();

    /**
     * @return an {@link Optional} of {@link Integer} that's non-empty if the user is in a party
     * @since 1.3.0
     */
    Optional<String> partyId();

    /**
     * @return an {@link Optional} of {@link Integer} that's non-empty if the user is in a party
     * @since 1.3.0
     */
    Optional<String> partyKey();

    /**
     * @return an {@link Optional} of {@link Platform} that represents the platform the user is
     * updating their presence from
     * @since 1.3.0
     */
    Optional<Platform> platform();

    /**
     * @return an {@link Optional} of {@link Application} that represents the application the user is
     * updating their presence from
     * @since 1.3.0
     */
    Optional<Application> application();

    /**
     * @return an {@link Optional} of {@link Integer} that's non-empty if the user is in a game
     * @since 1.3.0
     */
    Optional<Integer> remainingPlayerCount();
}
