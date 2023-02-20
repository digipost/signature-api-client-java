package no.digipost.signature.client.core.internal.http;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ResponseStatusTest {

    @Test
    void resolveSuccessfulHttpStatus() {
        assertThat(ResponseStatus.fromHttpStatusCode(200).get().family(), is(StatusCodeFamily.SUCCESSFUL));
        assertThat(ResponseStatus.fromHttpStatusCode(204).get().family(), is(StatusCodeFamily.SUCCESSFUL));
    }

    @Test
    void resolveClientError() {
        assertThat(ResponseStatus.fromHttpStatusCode(400).get().family(), is(StatusCodeFamily.CLIENT_ERROR));
        assertThat(ResponseStatus.fromHttpStatusCode(404).get().family(), is(StatusCodeFamily.CLIENT_ERROR));
        assertThat(ResponseStatus.fromHttpStatusCode(409).get().family(), is(StatusCodeFamily.CLIENT_ERROR));
        assertThat(ResponseStatus.fromHttpStatusCode(422).get().family(), is(StatusCodeFamily.CLIENT_ERROR));
    }

}
