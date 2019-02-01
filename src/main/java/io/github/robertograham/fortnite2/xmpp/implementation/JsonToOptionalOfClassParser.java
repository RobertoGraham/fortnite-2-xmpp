package io.github.robertograham.fortnite2.xmpp.implementation;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.adapter.JsonbAdapter;
import java.util.Optional;

enum JsonToOptionalOfClassParser {

    INSTANCE(
        DefaultSession.Adapter.INSTANCE
    );

    private final Jsonb jsonb;

    JsonToOptionalOfClassParser(final JsonbAdapter... jsonbAdapters) {
        jsonb = JsonbBuilder.create(new JsonbConfig()
            .withAdapters(jsonbAdapters));
    }

    <T> Optional<T> parseJsonToOptionalOfClass(final Class<T> tClass, final String jsonString) {
        return Optional.ofNullable(jsonString)
            .filter(nonNullJsonString -> !nonNullJsonString.isBlank())
            .map(nonNullNonEmptyJsonString -> jsonb.fromJson(nonNullNonEmptyJsonString, tClass));
    }
}
