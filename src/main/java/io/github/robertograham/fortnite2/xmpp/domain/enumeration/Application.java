package io.github.robertograham.fortnite2.xmpp.domain.enumeration;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Friends of the authenticated user will see the user to
 * be on this application
 */
public enum Application {

    EPIC_GAMES_LAUNCHER("launcher"),
    FORTNITE_CLIENT("Fortnite");

    private final String code;

    Application(final String code) {
        this.code = code;
    }

    public static Optional<Application> fromCode(final String code) {
        Objects.requireNonNull(code, "code cannot be null");
        return Arrays.stream(Application.values())
            .filter((final var application) -> application.code().equals(code))
            .findFirst();
    }

    public String code() {
        return code;
    }
}
