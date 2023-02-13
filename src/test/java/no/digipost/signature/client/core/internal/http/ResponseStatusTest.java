package no.digipost.signature.client.core.internal.http;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;

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

    @Test
    public void correctEqualsHashCodeForAnyResolvedStatus() {
        qt()
        .forAll(integers().between(0, 1000))
        .checkAssert(anyStatusCode -> {
            assertThat(ResponseStatus.resolve(anyStatusCode).get(), is(ResponseStatus.resolve(anyStatusCode).get()));
            assertThat(ResponseStatus.resolve(anyStatusCode).get(), not(ResponseStatus.resolve(anyStatusCode + 1).get()));
        });
    }

}
