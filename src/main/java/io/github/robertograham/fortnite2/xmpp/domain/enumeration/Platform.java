package io.github.robertograham.fortnite2.xmpp.domain.enumeration;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Friends of the authenticated user will see the user to
 * be logged in on this platform
 */
public enum Platform {

    XBOX("XBL"),
    PLAYSTATION("PSN"),
    WINDOWS("WIN"),
    MACOS("MAC"),
    ANDROID("AND"),
    IOS("IOS"),
    NINTENDO_SWITCH("SWT");

    private final String code;

    Platform(final String code) {
        this.code = code;
    }

    public static Optional<Platform> fromCode(final String code) {
        Objects.requireNonNull(code, "code cannot be null");
        return Arrays.stream(Platform.values())
            .filter((final var platform) -> platform.code().equals(code))
            .findFirst();
    }

    public String code() {
        return code;
    }
}
