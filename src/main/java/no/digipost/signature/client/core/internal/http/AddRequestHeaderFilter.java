package no.digipost.signature.client.core.internal.http;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

import static javax.ws.rs.Priorities.HEADER_DECORATOR;


@Priority(HEADER_DECORATOR)
public class AddRequestHeaderFilter implements ClientRequestFilter {

    private final String headerName;
    private final String value;

    public AddRequestHeaderFilter(String headerName, String value) {
        this.headerName = headerName;
        this.value = value;
    }

	@Override
	public void filter(ClientRequestContext clientRequestContext) throws IOException {
		clientRequestContext.getHeaders().add(headerName, value);
	}

}
