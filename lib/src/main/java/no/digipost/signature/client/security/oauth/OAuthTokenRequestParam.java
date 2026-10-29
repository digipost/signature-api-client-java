package no.digipost.signature.client.security.oauth;

import java.util.function.BiFunction;

public interface OAuthTokenRequestParam {

    String name();

    String value();

    default <TUPLE> TUPLE to(BiFunction<String, String, TUPLE> tupleConstructor) {
        return tupleConstructor.apply(name(), value());
    }

}
