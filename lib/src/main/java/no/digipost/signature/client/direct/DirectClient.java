package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLDirectSignatureJobRequest;
import no.digipost.signature.api.xml.XMLDirectSignatureJobResponse;
import no.digipost.signature.api.xml.XMLDirectSignatureJobStatusResponse;
import no.digipost.signature.api.xml.XMLDirectSignerResponse;
import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.asice.CreateASiCE;
import no.digipost.signature.client.asice.DocumentBundle;
import no.digipost.signature.client.asice.manifest.CreateDirectManifest;
import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.DeleteDocumentsUrl;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.ResponseInputStream;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.internal.ApiFlow;
import no.digipost.signature.client.core.internal.ClientHelper;
import no.digipost.signature.client.core.internal.DownloadHelper;
import no.digipost.signature.client.core.internal.JobStatusResponse;
import no.digipost.signature.client.core.internal.MaySpecifySender;
import no.digipost.signature.client.core.internal.http.SignatureServiceRoot;

import static no.digipost.signature.client.direct.DirectJobStatusResponse.noUpdatedStatus;
import static no.digipost.signature.client.direct.JaxbEntityMapping.fromJaxb;
import static no.digipost.signature.client.direct.JaxbEntityMapping.toJaxb;
import static org.apache.hc.core5.http.ContentType.APPLICATION_OCTET_STREAM;
import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;

public class DirectClient {

    private final ClientHelper client;
    private final DownloadHelper download;
    private final CreateASiCE<DirectJob> aSiCECreator;
    private final MaySpecifySender defaultSender;

    public DirectClient(ClientConfiguration config) {
        this.defaultSender = config.getDefaultSender();
        SignatureServiceRoot serviceRoot = SignatureServiceRoot.from(config);
        this.client = new ClientHelper(serviceRoot, config.defaultHttpClient());
        this.download = new DownloadHelper(serviceRoot, config.httpClientForDocumentDownloads());
        this.aSiCECreator = new CreateASiCE<>(new CreateDirectManifest(defaultSender), config);
    }

    public DirectJobResponse create(DirectJob job) {
        Sender sender = job.resolveSenderWithFallbackTo(defaultSender);
        DocumentBundle documentBundle = aSiCECreator.createASiCE(job);
        XMLDirectSignatureJobRequest createJobRequest = toJaxb(job, sender.getPollingQueue());
        XMLDirectSignatureJobResponse createdJob = client.sendSignatureJobRequest(ApiFlow.DIRECT, createJobRequest, documentBundle, sender);
        return fromJaxb(createdJob);
    }

    public DirectSignerResponse requestNewRedirectUrl(WithSignerUrl request) {
        XMLDirectSignerResponse updatedSigner = client.requestNewRedirectUrl(request);
        return DirectSignerResponse.fromJaxb(updatedSigner);
    }


    /**
     * Get the current status for the given {@link StatusReference}, which references the status for a specific job.
     * When processing of the status is complete (e.g. retrieving {@link #getPAdES(PAdESReference) PAdES} and/or
     * {@link #getXAdES(XAdESReference) XAdES} documents for a {@link DirectJobStatus#COMPLETED_SUCCESSFULLY completed} job
     * where all signers have {@link SignerStatus#SIGNED signed} their documents),
     * the returned status must be {@link #confirm(DirectJobStatusResponse) confirmed}.
     *
     * @param statusReference the reference to the status of a specific job.
     * @return the {@link DirectJobStatusResponse} for the job referenced by the given {@link StatusReference},
     *         never {@code null}.
     */
    public DirectJobStatusResponse getStatus(StatusReference statusReference) {
        XMLDirectSignatureJobStatusResponse xmlSignatureJobStatusResponse = client.sendSignatureJobStatusRequest(statusReference.getStatusUrl());
        return fromJaxb(xmlSignatureJobStatusResponse, null);
    }

    /**
     * If there is a job with an updated {@link DirectJobStatus status}, the returned object contains
     * necessary information to act on the status change. The returned object can be queried using
     * {@link DirectJobStatusResponse#is(DirectJobStatus) .is(}{@link DirectJobStatus#NO_CHANGES NO_CHANGES)}
     * to determine if there has been a status change. When processing of the status change is complete, (e.g. retrieving
     * {@link #getPAdES(PAdESReference) PAdES} and/or {@link #getXAdES(XAdESReference) XAdES} documents for a
     * {@link DirectJobStatus#COMPLETED_SUCCESSFULLY completed} job where all signers have {@link SignerStatus#SIGNED signed} their documents,
     * the returned status must be {@link #confirm(DirectJobStatusResponse) confirmed}.
     * <p>
     * Only jobs with {@link DirectJob.Builder#retrieveStatusBy(StatusRetrievalMethod) status retrieval method} set
     * to {@link StatusRetrievalMethod#POLLING POLLING} will be returned.
     *
     * @return the changed status for a job, or an instance indicating {@link DirectJobStatus#NO_CHANGES no changes},
     *         never {@code null}.
     */
    public DirectJobStatusResponse getStatusChange() {
        return getStatusChange(null);
    }

    /**
     * If there is a job with an updated {@link DirectJobStatus status}, the returned object contains
     * necessary information to act on the status change. The returned object can be queried using
     * {@link DirectJobStatusResponse#is(DirectJobStatus) .is(}{@link DirectJobStatus#NO_CHANGES NO_CHANGES)}
     * to determine if there has been a status change. When processing of the status change is complete, (e.g. retrieving
     * {@link #getPAdES(PAdESReference) PAdES} and/or {@link #getXAdES(XAdESReference) XAdES} documents for a
     * {@link DirectJobStatus#COMPLETED_SUCCESSFULLY completed} job where all signers have {@link SignerStatus#SIGNED signed} their documents,
     * the returned status must be {@link #confirm(DirectJobStatusResponse) confirmed}.
     * <p>
     * Only jobs with {@link DirectJob.Builder#retrieveStatusBy(StatusRetrievalMethod) status retrieval method} set
     * to {@link StatusRetrievalMethod#POLLING POLLING} will be returned.
     *
     * @return the changed status for a job, or an instance indicating {@link DirectJobStatus#NO_CHANGES no changes},
     *         never {@code null}.
     */
    public DirectJobStatusResponse getStatusChange(Sender sender) {
        JobStatusResponse<XMLDirectSignatureJobStatusResponse> statusChangeResponse = client
                .getStatusChange(ApiFlow.DIRECT, MaySpecifySender.ofNullable(sender).resolveSenderWithFallbackTo(defaultSender));
        if (statusChangeResponse.gotStatusChange()) {
            return fromJaxb(statusChangeResponse.getStatusResponse(), statusChangeResponse.getNextPermittedPollTime());
        } else {
            return noUpdatedStatus(statusChangeResponse.getNextPermittedPollTime());
        }
    }


    /**
     * Confirms that the status retrieved from {@link #getStatus(StatusReference)} or {@link #getStatusChange()} is received.
     * If the confirmed {@link DirectJobStatus} is a terminal status
     * (i.e. {@link DirectJobStatus#COMPLETED_SUCCESSFULLY completed} or {@link DirectJobStatus#FAILED failed}),
     * the Signature service may make the job's associated resources unavailable through the API when
     * receiving the confirmation. Calling this method for a response with no {@link ConfirmationReference}
     * has no effect.
     * <p>
     * If the status is retrieved using {@link #getStatusChange() the polling method}, failing to confirm the
     * received response may cause subsequent statuses for the same job to be reported as "changed", even
     * though the status has not changed.
     *
     * @param receivedStatusResponse the updated status retrieved from {@link #getStatus(StatusReference)}.
     */
    public void confirm(DirectJobStatusResponse receivedStatusResponse) {
        client.confirm(receivedStatusResponse);
    }


    public ResponseInputStream getXAdES(XAdESReference xAdESReference) {
        return download.getDataStream(xAdESReference.getxAdESUrl(), APPLICATION_XML);
    }

    public ResponseInputStream getPAdES(PAdESReference pAdESReference) {
        return download.getDataStream(pAdESReference.getpAdESUrl(), APPLICATION_OCTET_STREAM, APPLICATION_XML);
    }

    public void deleteDocuments(DeleteDocumentsUrl deleteDocumentsUrl) {
        client.deleteDocuments(deleteDocumentsUrl);
    }

}
