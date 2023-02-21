package no.digipost.signature.client.core.internal.http;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static no.digipost.signature.client.core.internal.http.StatusCode.Family.CLIENT_ERROR;
import static no.digipost.signature.client.core.internal.http.StatusCode.Family.SUCCESSFUL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class StatusCodeTest {

    @Test
    void resolveSuccessfulHttpStatus() {
        assertThat(StatusCode.OK, where(StatusCode::family, is(SUCCESSFUL)));
        assertThat(StatusCode.from(200), where(StatusCode::family, is(SUCCESSFUL)));
        assertThat(StatusCode.from(204), where(StatusCode::family, is(SUCCESSFUL)));
    }

    @Test
    void resolveClientError() {
        assertThat(StatusCode.from(400), where(StatusCode::family, is(CLIENT_ERROR)));
        assertThat(StatusCode.from(404), where(StatusCode::family, is(CLIENT_ERROR)));
        assertThat(StatusCode.from(409), where(StatusCode::family, is(CLIENT_ERROR)));
        assertThat(StatusCode.from(422), where(StatusCode::family, is(CLIENT_ERROR)));
    }

    @Test
    void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(StatusCode.class).verify();
    }

}
