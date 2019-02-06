package io.github.robertograham.fortnite2.xmpp.implementation;

import io.github.robertograham.fortnite2.xmpp.domain.Session;
import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Application;
import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Platform;

import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

final class DefaultSession implements Session {

    private final String id;
    private final String status;
    private final boolean playing;
    private final boolean joinable;
    private final boolean voiceSupport;
    private final Optional<Integer> partyMemberCount;
    private final Optional<Integer> maxPartyMemberCount;
    private final Optional<String> partyId;
    private final Optional<String> partyKey;
    private final Optional<Platform> platform;
    private final Optional<Application> application;
    private final Optional<Integer> remainingPlayerCount;

    private DefaultSession(final String id,
                           final String status,
                           final boolean playing,
                           final boolean joinable,
                           final boolean voiceSupport,
                           final Optional<Integer> partyMemberCount,
                           final Optional<Integer> maxPartyMemberCount,
                           final Optional<String> partyId,
                           final Optional<String> partyKey,
                           final Optional<Platform> platform,
                           final Optional<Application> application,
                           final Optional<Integer> remainingPlayerCount) {
        this.id = id;
        this.status = status;
        this.playing = playing;
        this.joinable = joinable;
        this.voiceSupport = voiceSupport;
        this.partyMemberCount = partyMemberCount;
        this.maxPartyMemberCount = maxPartyMemberCount;
        this.partyId = partyId;
        this.partyKey = partyKey;
        this.platform = platform;
        this.application = application;
        this.remainingPlayerCount = remainingPlayerCount;
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
    public Optional<Integer> partyMemberCount() {
        return partyMemberCount;
    }

    @Override
    public Optional<Integer> maxPartyMemberCount() {
        return maxPartyMemberCount;
    }

    @Override
    public Optional<String> partyId() {
        return partyId;
    }

    @Override
    public Optional<String> partyKey() {
        return partyKey;
    }

    @Override
    public Optional<Platform> platform() {
        return platform;
    }

    @Override
    public Optional<Application> application() {
        return application;
    }

    @Override
    public Optional<Integer> remainingPlayerCount() {
        return remainingPlayerCount;
    }

    @Override
    public String toString() {
        return "DefaultSession{" +
            "id='" + id + '\'' +
            ", status='" + status + '\'' +
            ", playing=" + playing +
            ", joinable=" + joinable +
            ", voiceSupport=" + voiceSupport +
            ", partyMemberCount=" + partyMemberCount +
            ", maxPartyMemberCount=" + maxPartyMemberCount +
            ", partyId=" + partyId +
            ", partyKey=" + partyKey +
            ", platform=" + platform +
            ", application=" + application +
            ", remainingPlayerCount=" + remainingPlayerCount +
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
            status.equals(defaultSession.status) &&
            partyMemberCount.equals(defaultSession.partyMemberCount) &&
            maxPartyMemberCount.equals(defaultSession.maxPartyMemberCount) &&
            partyId.equals(defaultSession.partyId) &&
            partyKey.equals(defaultSession.partyKey) &&
            platform.equals(defaultSession.platform) &&
            application.equals(defaultSession.application) &&
            remainingPlayerCount.equals(defaultSession.remainingPlayerCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, playing, joinable, voiceSupport, partyMemberCount, maxPartyMemberCount, partyId, partyKey, platform, application, remainingPlayerCount);
    }

    enum Adapter implements JsonbAdapter<DefaultSession, JsonObject> {

        INSTANCE;

        @Override
        public JsonObject adaptToJson(final DefaultSession defaultSession) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DefaultSession adaptFromJson(final JsonObject jsonObject) {
            final var propertiesJsonObjectOptional = Optional.ofNullable(jsonObject.getJsonObject("Properties"));
            final var partyInfoJsonObjectOptional = propertiesJsonObjectOptional.map((final var propertiesJsonObject) -> propertiesJsonObject.getJsonObject("party.joininfodata.286331153_j"));
            return new DefaultSession(
                jsonObject.getString("SessionId"),
                jsonObject.getString("Status"),
                jsonObject.getBoolean("bIsPlaying"),
                jsonObject.getBoolean("bIsJoinable"),
                jsonObject.getBoolean("bHasVoiceSupport"),
                getOptionalValueFromJsonObject(propertiesJsonObjectOptional, "Event_PartySize_s", JsonObject::getString)
                    .map(Integer::parseInt),
                getOptionalValueFromJsonObject(propertiesJsonObjectOptional, "Event_PartyMaxSize_s", JsonObject::getString)
                    .map(Integer::parseInt),
                getOptionalValueFromJsonObject(partyInfoJsonObjectOptional, "partyId", JsonObject::getString),
                getOptionalValueFromJsonObject(partyInfoJsonObjectOptional, "key", JsonObject::getString),
                getOptionalValueFromJsonObject(partyInfoJsonObjectOptional, "sourcePlatform", JsonObject::getString)
                    .flatMap(Platform::fromCode),
                getOptionalValueFromJsonObject(partyInfoJsonObjectOptional, "appId", JsonObject::getString)
                    .flatMap(Application::fromCode),
                getOptionalValueFromJsonObject(propertiesJsonObjectOptional, "Event_PlayersAlive_s", JsonObject::getString)
                    .map(Integer::parseInt)
            );
        }

        private <T> Optional<T> getOptionalValueFromJsonObject(final Optional<JsonObject> jsonObjectOptional,
                                                               final String valueKey,
                                                               final BiFunction<JsonObject, String, T> jsonObjectValueAccessor) {
            return jsonObjectOptional.filter((final var propertiesJsonObject) -> propertiesJsonObject.containsKey(valueKey))
                .map((final var propertiesJsonObject) -> jsonObjectValueAccessor.apply(propertiesJsonObject, valueKey));
        }
    }
}
