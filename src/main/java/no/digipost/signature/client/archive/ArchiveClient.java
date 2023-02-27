package no.digipost.signature.client.archive;

import no.digipost.signature.client.core.ResponseInputStream;
import no.digipost.signature.client.core.internal.ClientHelper;
import no.digipost.signature.client.core.internal.http.HttpIntegrationConfiguration;
import no.digipost.signature.client.core.internal.http.SignatureServiceRoot;

public class ArchiveClient {

    private ClientHelper client;

    public ArchiveClient(HttpIntegrationConfiguration httpIntegrationConfig) {
        this.client = new ClientHelper(new SignatureServiceRoot(httpIntegrationConfig.getServiceRoot()), httpIntegrationConfig.httpClient());
    }

    public ResponseInputStream getPAdES(ArchiveOwner owner, String id) {
        return client.getDataStream(owner.getOrganizationNumber() + "/archive/documents/" + id + "/pades");
    }

}
