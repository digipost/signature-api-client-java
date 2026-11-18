package no.digipost.signature.client.security.oauth;

import no.digipost.signature.client.core.internal.NamedValue;

public final class ClientId extends NamedValue<String> implements OAuthTokenRequestParam {
    public ClientId(String value) {
        super("client_id", value);
    }
}
