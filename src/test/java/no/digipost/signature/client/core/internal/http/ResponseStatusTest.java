package no.digipost.signature.client.core.internal.http;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ResponseStatusTest {

    @Test
    public void resolveSuccessfulHttpStatus() {
        assertThat(ResponseStatus.resolve(200).get().family(), is(StatusCodeFamily.SUCCESSFUL));
        assertThat(ResponseStatus.resolve(204).get().family(), is(StatusCodeFamily.SUCCESSFUL));
    }

    @Test
    public void resolveClientError() {
        assertThat(ResponseStatus.resolve(400).get().family(), is(StatusCodeFamily.CLIENT_ERROR));
        assertThat(ResponseStatus.resolve(404).get().family(), is(StatusCodeFamily.CLIENT_ERROR));
        assertThat(ResponseStatus.resolve(409).get().family(), is(StatusCodeFamily.CLIENT_ERROR));
        assertThat(ResponseStatus.resolve(422).get().family(), is(StatusCodeFamily.CLIENT_ERROR));
    }

}
