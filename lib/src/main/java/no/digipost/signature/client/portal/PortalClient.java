package no.digipost.signature.client.portal;

import no.digipost.signature.api.xml.XMLPortalSignatureJobRequest;
import no.digipost.signature.api.xml.XMLPortalSignatureJobResponse;
import no.digipost.signature.api.xml.XMLPortalSignatureJobStatusChangeResponse;
import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.asice.CreateASiCE;
import no.digipost.signature.client.asice.DocumentBundle;
import no.digipost.signature.client.asice.manifest.CreatePortalManifest;
import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.DeleteDocumentsUrl;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.ResponseInputStream;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.internal.ApiFlow;
import no.digipost.signature.client.core.internal.Cancellable;
import no.digipost.signature.client.core.internal.ClientHelper;
import no.digipost.signature.client.core.internal.DownloadHelper;
import no.digipost.signature.client.core.internal.JobStatusResponse;
import no.digipost.signature.client.core.internal.MaySpecifySender;
import no.digipost.signature.client.core.internal.http.SignatureServiceRoot;

import static no.digipost.signature.client.portal.JaxbEntityMapping.fromJaxb;
import static no.digipost.signature.client.portal.JaxbEntityMapping.toJaxb;
import static no.digipost.signature.client.portal.PortalJobStatusChanged.noUpdatedStatus;
import static org.apache.hc.core5.http.ContentType.APPLICATION_OCTET_STREAM;
import static org.apache.hc.core5.http.ContentType.APPLICATION_XML;

public class PortalClient {

    private final ClientHelper client;
    private final DownloadHelper download;
    private final CreateASiCE<PortalJob> aSiCECreator;
    private final MaySpecifySender defaultSender;

    public PortalClient(ClientConfiguration config) {
        this.defaultSender = config.getDefaultSender();
        SignatureServiceRoot serviceRoot = SignatureServiceRoot.from(config);
        this.client = new ClientHelper(serviceRoot, config.defaultHttpClient());
        this.download = new DownloadHelper(serviceRoot, config.httpClientForDocumentDownloads());
        this.aSiCECreator = new CreateASiCE<>(new CreatePortalManifest(config.getDefaultSender(), config.getClock()), config);
    }


    public PortalJobResponse create(PortalJob job) {
        Sender sender = job.resolveSenderWithFallbackTo(defaultSender);
        DocumentBundle documentBundle = aSiCECreator.createASiCE(job);
        XMLPortalSignatureJobRequest createJobRequest = toJaxb(job, sender.getPollingQueue());
        XMLPortalSignatureJobResponse createdJobResponse = client.sendSignatureJobRequest(ApiFlow.PORTAL, createJobRequest, documentBundle, sender);
        return fromJaxb(createdJobResponse);
    }


    /**
     * If there is a job with an updated {@link PortalJobStatus status}, the returned object contains
     * necessary information to act on the status change. The returned object can be queried using
     * {@link PortalJobStatusChanged#is(PortalJobStatus) .is(}{@link PortalJobStatus#NO_CHANGES NO_CHANGES)}
     * to determine if there has been a status change. When processing of the status change is complete, (e.g. retrieving
     * {@link #getPAdES(PAdESReference) PAdES} and/or {@link #getXAdES(XAdESReference) XAdES} documents for a
     * {@link PortalJobStatus#COMPLETED_SUCCESSFULLY completed} job where all signers have {@link SignatureStatus signed} their documents),
     * the returned status must be {@link #confirm(PortalJobStatusChanged) confirmed}.
     *
     * @return the changed status for a job, or an instance indicating {@link PortalJobStatus#NO_CHANGES no changes},
     *         never {@code null}.
     */
    public PortalJobStatusChanged getStatusChange() {
        return getStatusChange(null);
    }

    /**
     * If there is a job with an updated {@link PortalJobStatus status}, the returned object contains
     * necessary information to act on the status change. The returned object can be queried using
     * {@link PortalJobStatusChanged#is(PortalJobStatus) .is(}{@link PortalJobStatus#NO_CHANGES NO_CHANGES)}
     * to determine if there has been a status change. When processing of the status change is complete, (e.g. retrieving
     * {@link #getPAdES(PAdESReference) PAdES} and/or {@link #getXAdES(XAdESReference) XAdES} documents for a
     * {@link PortalJobStatus#COMPLETED_SUCCESSFULLY completed} job where all signers have {@link SignatureStatus signed} their documents),
     * the returned status must be {@link #confirm(PortalJobStatusChanged) confirmed}.
     *
     * @return the changed status for a job, or an instance indicating {@link PortalJobStatus#NO_CHANGES no changes},
     *         never {@code null}.
     */

    public PortalJobStatusChanged getStatusChange(Sender sender) {
        JobStatusResponse<XMLPortalSignatureJobStatusChangeResponse> statusChangeResponse = client
                .getStatusChange(ApiFlow.PORTAL, MaySpecifySender.ofNullable(sender).resolveSenderWithFallbackTo(defaultSender));
        if (statusChangeResponse.gotStatusChange()) {
            return JaxbEntityMapping.fromJaxb(statusChangeResponse);
        } else {
            return noUpdatedStatus(statusChangeResponse.getNextPermittedPollTime());
        }
    }


    /**
     * Confirms that the status retrieved from {@link #getStatusChange()} is received and may
     * be discarded by the Signature service and not retrieved again. Calling this method on
     * a status update with no {@link ConfirmationReference} has no effect.
     *
     * @param receivedStatusChanged the updated status retrieved from {@link #getStatusChange()}.
     */
    public void confirm(PortalJobStatusChanged receivedStatusChanged) {
        client.confirm(receivedStatusChanged);
    }

    public void cancel(Cancellable cancellable) {
        client.cancel(cancellable);
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
