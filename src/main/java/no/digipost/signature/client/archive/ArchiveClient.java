package no.digipost.signature.client.archive;

import no.digipost.signature.client.core.ResponseInputStream;
import no.digipost.signature.client.core.internal.ClientHelper;
import no.digipost.signature.client.core.internal.http.HttpIntegrationConfiguration;
import no.digipost.signature.client.core.internal.http.SignatureHttpClientFactory;

public class ArchiveClient {

    private ClientHelper client;

    public ArchiveClient(HttpIntegrationConfiguration httpIntegrationConfig) {
        this.client = new ClientHelper(SignatureHttpClientFactory.create(httpIntegrationConfig));
    }

    public ResponseInputStream getPAdES(ArchiveOwner owner, String id) {
        return client.getDataStream(owner.getOrganizationNumber() + "/archive/documents/" + id + "/pades");
    }

}
