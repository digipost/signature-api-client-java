package no.digipost.signature.client.direct;

import org.junit.Test;

import static co.unruly.matchers.Java8Matchers.where;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DirectJobResponseTest {

    @Test
    public void findSpecificSignerInResponse() {
        DirectJobResponse response = new DirectJobResponse(42, null, "http://status", asList(
                new DirectSignerResponse("11111111111", null, null, null),
                new DirectSignerResponse("22222222222", null, null, null),
                new DirectSignerResponse(null, "id-42", null, null)));

        assertThat(response.getSignerIdentifiedBy("id-42"), where(DirectSignerResponse::getCustomIdentifier, is("id-42")));
        assertThat(response.getSignerIdentifiedBy("11111111111"), where(DirectSignerResponse::getPersonalIdentificationNumber, is("11111111111")));
    }

}
