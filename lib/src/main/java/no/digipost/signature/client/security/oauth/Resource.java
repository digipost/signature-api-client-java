package no.digipost.signature.client.security.oauth;

import no.digipost.signature.client.core.internal.NamedValue;

public final class Resource extends NamedValue<String> implements OAuthTokenRequestParam {
    public Resource(String value) {
        super("resource", value);
    }
}
