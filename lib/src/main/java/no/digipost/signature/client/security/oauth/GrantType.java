package no.digipost.signature.client.security.oauth;

import no.digipost.signature.client.core.internal.NamedValue;

public final class GrantType extends NamedValue<String> implements OAuthTokenRequestParam {

    public static final GrantType CLIENT_CREDENTIALS = new GrantType("client_credentials");

    public GrantType(String value) {
        super("grant_type", value);
    }

}
