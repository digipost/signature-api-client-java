package no.digipost.signature.client.core.internal.http;

import org.junit.jupiter.api.Test;

import static no.digipost.signature.client.core.internal.http.StatusCode.CONFLICT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class ResponseStatusTest {

    @Test
    void throwIfNotExpected() {
        ResponseStatus conflictResponse = ResponseStatus.fromHttpStatusCode(CONFLICT.value());
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> conflictResponse.throwIf(CONFLICT, s -> new IllegalStateException(s.toString())));
        assertThat(thrown, where(Throwable::getMessage, containsString(CONFLICT.toString())));
    }

}
