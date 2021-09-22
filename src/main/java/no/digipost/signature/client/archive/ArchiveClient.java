package no.digipost.signature.client.archive;

import no.digipost.signature.client.core.ResponseInputStream;
import no.digipost.signature.client.core.internal.ClientHelper;
import no.digipost.signature.client.core.internal.http.HttpIntegrationConfiguration;
import no.digipost.signature.client.core.internal.http.SignatureHttpClientFactory;

import java.io.InputStream;
import java.util.Optional;

public class ArchiveClient {

    private ClientHelper client;

    public ArchiveClient(HttpIntegrationConfiguration httpIntegrationConfig) {
        this.client = new ClientHelper(SignatureHttpClientFactory.create(httpIntegrationConfig), Optional.empty());
    }

    public ResponseInputStream getPAdES(ArchiveOwner owner, String id) {
        return client.getDataStream(root -> root.path(owner.getOrganizationNumber()).path("archive/documents/").path(id).path("pades"));
    }

}
