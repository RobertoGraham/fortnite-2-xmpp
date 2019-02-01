package io.github.robertograham.fortnite2.xmpp.implementation;

import io.github.robertograham.fortnite2.xmpp.domain.Session;

import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;
import java.util.Objects;

final class DefaultSession implements Session {

    private final String id;
    private final String status;
    private final boolean playing;
    private final boolean joinable;
    private final boolean voiceSupport;

    private DefaultSession(final String id,
                           final String status,
                           final boolean playing,
                           final boolean joinable,
                           final boolean voiceSupport) {
        this.id = id;
        this.status = status;
        this.playing = playing;
        this.joinable = joinable;
        this.voiceSupport = voiceSupport;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public boolean isJoinable() {
        return joinable;
    }

    @Override
    public boolean hasVoiceSupport() {
        return voiceSupport;
    }

    @Override
    public String toString() {
        return "DefaultSession{" +
            "id='" + id + '\'' +
            ", status='" + status + '\'' +
            ", playing=" + playing +
            ", joinable=" + joinable +
            ", voiceSupport=" + voiceSupport +
            '}';
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (!(object instanceof DefaultSession))
            return false;
        final var defaultSession = (DefaultSession) object;
        return playing == defaultSession.playing &&
            joinable == defaultSession.joinable &&
            voiceSupport == defaultSession.voiceSupport &&
            id.equals(defaultSession.id) &&
            status.equals(defaultSession.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, playing, joinable, voiceSupport);
    }

    enum Adapter implements JsonbAdapter<DefaultSession, JsonObject> {

        INSTANCE;

        @Override
        public JsonObject adaptToJson(final DefaultSession defaultSession) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DefaultSession adaptFromJson(final JsonObject jsonObject) {
            return new DefaultSession(
                jsonObject.getString("SessionId"),
                jsonObject.getString("Status"),
                jsonObject.getBoolean("bIsPlaying"),
                jsonObject.getBoolean("bIsJoinable"),
                jsonObject.getBoolean("bHasVoiceSupport")
            );
        }
    }
}
