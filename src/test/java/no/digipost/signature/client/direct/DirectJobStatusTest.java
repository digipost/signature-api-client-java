package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLDirectSignatureJobStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class DirectJobStatusTest {

    @Test
    public void ableToConvertAllStatusesFromXsd() {
        List<DirectJobStatus> convertedStatuses = new ArrayList<>();
        for (XMLDirectSignatureJobStatus xmlStatus : XMLDirectSignatureJobStatus.values()) {
            convertedStatuses.add(DirectJobStatus.fromXmlType(xmlStatus));
        }
        assertThat(convertedStatuses, hasSize(XMLDirectSignatureJobStatus.values().length));
    }
}
