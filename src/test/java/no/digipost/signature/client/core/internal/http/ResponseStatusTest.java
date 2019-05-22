package no.digipost.signature.client.core.internal.http;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import no.digipost.signature.client.core.internal.http.ResponseStatus.Custom;
import no.digipost.signature.client.core.internal.http.ResponseStatus.Unknown;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response.Status;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;

public class ResponseStatusTest {

    @Test
    public void resolveStandardHttpStatus() {
        assertThat(ResponseStatus.resolve(200), is(Status.OK));
    }

    @Test
    public void resolveCustomHttpStatus() {
        assertThat(ResponseStatus.resolve(422), is(Custom.UNPROCESSABLE_ENTITY));
    }

    @Test
    public void resolveUnknownHttpStatus() {
        assertThat(ResponseStatus.resolve(478), is(ResponseStatus.unknown(478)));
    }

    @Test
    public void correctEqualsHashCodeForAnyResolvedStatus() {
        qt()
        .forAll(integers().between(0, 1000))
        .checkAssert(anyStatusCode -> {
            assertThat(ResponseStatus.resolve(anyStatusCode), is(ResponseStatus.resolve(anyStatusCode)));
            assertThat(ResponseStatus.resolve(anyStatusCode), not(ResponseStatus.resolve(anyStatusCode + 1)));
        });
    }

    @Test
    public void correctEqualsHashCodeForUnknownStatus() {
        EqualsVerifier.forClass(Unknown.class).suppress(Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }
}
