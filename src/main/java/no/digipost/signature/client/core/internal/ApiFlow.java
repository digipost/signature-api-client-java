package no.digipost.signature.client.core.internal;

import no.digipost.signature.api.xml.XMLDirectSignatureJobRequest;
import no.digipost.signature.api.xml.XMLDirectSignatureJobResponse;
import no.digipost.signature.api.xml.XMLDirectSignatureJobStatusResponse;
import no.digipost.signature.api.xml.XMLPortalSignatureJobRequest;
import no.digipost.signature.api.xml.XMLPortalSignatureJobResponse;
import no.digipost.signature.api.xml.XMLPortalSignatureJobStatusChangeResponse;
import no.digipost.signature.client.core.Sender;

public final class ApiFlow<CREATE_JOB_REQUEST, CREATE_JOB_RESPONSE, STATUS_RESPONSE> {

    public static final ApiFlow<XMLPortalSignatureJobRequest, XMLPortalSignatureJobResponse, XMLPortalSignatureJobStatusChangeResponse>
            PORTAL = new ApiFlow<>("portal/signature-jobs", XMLPortalSignatureJobResponse.class, XMLPortalSignatureJobStatusChangeResponse.class);

    public static final ApiFlow<XMLDirectSignatureJobRequest, XMLDirectSignatureJobResponse, XMLDirectSignatureJobStatusResponse>
            DIRECT = new ApiFlow<>("direct/signature-jobs", XMLDirectSignatureJobResponse.class, XMLDirectSignatureJobStatusResponse.class);


    public final Class<CREATE_JOB_RESPONSE> apiResponseType;
    public final Class<STATUS_RESPONSE> statusResponseType;
    private final String jobsApiPath;

    private ApiFlow(String jobsApiPath, Class<CREATE_JOB_RESPONSE> apiResponseType, Class<STATUS_RESPONSE> statusResponseType) {
        this.apiResponseType = apiResponseType;
        this.statusResponseType = statusResponseType;
        this.jobsApiPath = jobsApiPath;
    }

    String path(Sender sender) {
        return sender.getOrganizationNumber() + "/" + jobsApiPath;
    }
}
