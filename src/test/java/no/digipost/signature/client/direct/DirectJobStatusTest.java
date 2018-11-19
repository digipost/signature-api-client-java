package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLDirectSignatureJobStatus;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

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
