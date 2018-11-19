package no.digipost.signature.client.direct;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StatusReferenceTest {

    @Test
    public void buildsCorrectUrlWithToken() {
        String statusUrl = "https://statusqueryservice/status/?job=1337";
        String token = "abcdefgh";
        StatusReference statusReference = StatusReference.ofUrl(statusUrl).withStatusQueryToken(token);
        assertThat(statusReference.getStatusUrl(), is(statusUrl + "&" + StatusReference.STATUS_QUERY_TOKEN_PARAM_NAME + "=" + token));
    }
}
