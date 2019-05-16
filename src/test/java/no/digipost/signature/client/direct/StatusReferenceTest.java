package no.digipost.signature.client.direct;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StatusReferenceTest {

    @Test
    public void buildsCorrectUrlWithToken() {
        String statusUrl = "https://statusqueryservice/status/?job=1337";
        String token = "abcdefgh";
        StatusReference statusReference = StatusReference.ofUrl(statusUrl).withStatusQueryToken(token);
        assertThat(statusReference.getStatusUrl(), is(URI.create(statusUrl + "&" + StatusReference.STATUS_QUERY_TOKEN_PARAM_NAME + "=" + token)));
    }
}
