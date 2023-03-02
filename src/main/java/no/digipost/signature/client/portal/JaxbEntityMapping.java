package no.digipost.signature.client.portal;

import no.digipost.signature.api.xml.XMLPortalSignatureJobRequest;
import no.digipost.signature.api.xml.XMLPortalSignatureJobResponse;
import no.digipost.signature.api.xml.XMLPortalSignatureJobStatusChangeResponse;
import no.digipost.signature.api.xml.XMLSignature;
import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.DeleteDocumentsUrl;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.PollingQueue;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.internal.JobStatusResponse;

import java.util.ArrayList;
import java.util.List;

final class JaxbEntityMapping {

    static XMLPortalSignatureJobRequest toJaxb(PortalJob job, PollingQueue pollingQueue) {
        return new XMLPortalSignatureJobRequest()
                .withReference(job.getReference())
                .withPollingQueue(pollingQueue.value);
    }

    static PortalJobResponse fromJaxb(XMLPortalSignatureJobResponse xmlPortalSignatureJobResponse) {
        return new PortalJobResponse(
                xmlPortalSignatureJobResponse.getSignatureJobId(),
                xmlPortalSignatureJobResponse.getReference(),
                CancellationUrl.of(xmlPortalSignatureJobResponse.getCancellationUrl())
        );
    }


    static PortalJobStatusChanged fromJaxb(JobStatusResponse<XMLPortalSignatureJobStatusChangeResponse> statusChangeResponse) {
        XMLPortalSignatureJobStatusChangeResponse statusChange = statusChangeResponse.getStatusResponse();
        List<Signature> signatures = new ArrayList<>();
        for (XMLSignature xmlSignature : statusChange.getSignatures().getSignatures()) {
            signatures.add(new Signature(
                    xmlSignature.getPersonalIdentificationNumber(),
                    xmlSignature.getIdentifier(),
                    SignatureStatus.of(xmlSignature.getStatus().getValue()),
                    xmlSignature.getStatus().getSince().toInstant(),
                    XAdESReference.of(xmlSignature.getXadesUrl())
            ));
        }


        return new PortalJobStatusChanged(
                statusChange.getSignatureJobId(),
                statusChange.getReference(),
                PortalJobStatus.fromXmlType(statusChange.getStatus()),
                ConfirmationReference.of(statusChange.getConfirmationUrl()),
                CancellationUrl.of(statusChange.getCancellationUrl()),
                DeleteDocumentsUrl.of(statusChange.getDeleteDocumentsUrl()),
                PAdESReference.of(statusChange.getSignatures().getPadesUrl()),
                signatures,
                statusChangeResponse.getNextPermittedPollTime());
    }
}
