package io.github.robertograham.fortnite2.xmpp.implementation;

import io.github.robertograham.fortnite2.client.Fortnite;
import io.github.robertograham.fortnite2.xmpp.client.FortniteXmpp;

import java.util.Objects;

public final class DefaultFortniteXmpp implements FortniteXmpp {

    private final Fortnite fortnite;

    private DefaultFortniteXmpp(final Builder builder) {
        fortnite = builder.fortnite;
    }

    @Override
    public void close() {
    }

    public static final class Builder {

        private final Fortnite fortnite;

        private Builder(final Fortnite fortnite) {
            this.fortnite = fortnite;
        }

        public static Builder newInstance(final Fortnite fortnite) {
            Objects.requireNonNull(fortnite, "fortnite cannot be null");
            return new Builder(fortnite);
        }

        public FortniteXmpp build() {
            return new DefaultFortniteXmpp(this);
        }
    }
}
