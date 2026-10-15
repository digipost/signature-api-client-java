package no.digipost.signature.client.security.oauth;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public final class OAuthConfiguration {

    public interface ServiceSpecific {
        URI tokenEndpoint();
        Resource resource();
    }

    private final URI tokenEndpoint;
    private final List<OAuthTokenRequestParam> requestParameters;

    public OAuthConfiguration(ServiceSpecific serviceSpecific, ClientId clientId, Scope scope) {
        this(serviceSpecific.tokenEndpoint(), serviceSpecific.resource(), clientId, scope);
    }

    public OAuthConfiguration(URI tokenEndpoint, Resource resource, ClientId clientId, Scope scope) {
        this(tokenEndpoint, Arrays.asList(GrantType.CLIENT_CREDENTIALS, clientId, resource, scope));
    }

    public OAuthConfiguration(URI tokenEndpoint, Collection<? extends OAuthTokenRequestParam> requestParameters) {
        this.tokenEndpoint = tokenEndpoint;
        this.requestParameters = unmodifiableList(new ArrayList<>(requestParameters));
    }

    public URI tokenEndpoint() {
        return tokenEndpoint;
    }

    public List<OAuthTokenRequestParam> requestParameters() {
        return requestParameters;
    }

}
