package no.digipost.signature.client.security.oauth;

import no.digipost.signature.client.core.internal.NamedValue;

import static java.util.Objects.requireNonNull;

public final class Scope extends NamedValue<String> implements OAuthTokenRequestParam {

    /**
     * Construct OAuth scope from the account ID found in Digipost's self-service
     * portal for managing certificates and clients.
     * If you prefer, you can use the {@link Scope#Scope(String) constructor} to
     * specifiy the scope verbatim.
     */
    public static Scope fromAccountId(long accountId) {
        return fromAccountId(String.valueOf(accountId));
    }

    /**
     * Construct OAuth scope from the account ID found in Digipost's self-service
     * portal for managing certificates and clients.
     * If you prefer, you can use the {@link Scope#Scope(String) constructor} to
     * specifiy the scope verbatim.
     */
    public static Scope fromAccountId(String accountId) {
        return new Scope("signering:" + requireNonNull(accountId, "accountId"));
    }


    public Scope(String value) {
        super("scope", value);
    }

}
