package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLDirectSignatureJobRequest;
import no.digipost.signature.api.xml.XMLDirectSignatureJobResponse;
import no.digipost.signature.api.xml.XMLDirectSignatureJobStatusResponse;
import no.digipost.signature.api.xml.XMLExitUrls;
import no.digipost.signature.api.xml.XMLSignerSpecificUrl;
import no.digipost.signature.api.xml.XMLSignerStatus;
import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.DeleteDocumentsUrl;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.PollingQueue;
import no.digipost.signature.client.core.XAdESReference;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

final class JaxbEntityMapping {

    static XMLDirectSignatureJobRequest toJaxb(DirectJob signatureJob, PollingQueue pollingQueue) {
        return new XMLDirectSignatureJobRequest()
                .withReference(signatureJob.getReference())
                .withExitUrls(new XMLExitUrls()
                        .withCompletionUrl(signatureJob.getCompletionUrl())
                        .withRejectionUrl(signatureJob.getRejectionUrl())
                        .withErrorUrl(signatureJob.getErrorUrl())
                )
                .withStatusRetrievalMethod(signatureJob.getStatusRetrievalMethod().map(StatusRetrievalMethod::getXmlEnumValue).orElse(null))
                .withPollingQueue(pollingQueue.value);
    }

    static DirectJobResponse fromJaxb(XMLDirectSignatureJobResponse job) {
        List<DirectSignerResponse> signers = job.getSigners().stream().map(DirectSignerResponse::fromJaxb).collect(toList());

        return new DirectJobResponse(
                job.getSignatureJobId(),
                job.getReference(),
                job.getStatusUrl(),
                signers
        );
    }

    static DirectJobStatusResponse fromJaxb(XMLDirectSignatureJobStatusResponse statusResponse, Instant nextPermittedPollTime) {
        List<Signature> signatures = new ArrayList<>();
        for (XMLSignerStatus signerStatus : statusResponse.getStatuses()) {
            URI xAdESUrl = statusResponse.getXadesUrls().stream()
                    .filter(forSigner(signerStatus.getSigner()))
                    .findFirst()
                    .map(XMLSignerSpecificUrl::getValue)
                    .orElse(null);

            signatures.add(new Signature(
                    signerStatus.getSigner(),
                    SignerStatus.of(signerStatus.getValue()),
                    signerStatus.getSince().toInstant(),
                    XAdESReference.of(xAdESUrl)
            ));
        }

        return new DirectJobStatusResponse(
                statusResponse.getSignatureJobId(),
                statusResponse.getReference(),
                DirectJobStatus.fromXmlType(statusResponse.getSignatureJobStatus()),
                ConfirmationReference.of(statusResponse.getConfirmationUrl()),
                DeleteDocumentsUrl.of(statusResponse.getDeleteDocumentsUrl()),
                signatures,
                PAdESReference.of(statusResponse.getPadesUrl()),
                nextPermittedPollTime);
    }

    private static Predicate<XMLSignerSpecificUrl> forSigner(final String signer) {
        return url -> url.getSigner().equals(signer);
    }
}
