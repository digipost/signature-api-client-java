package no.digipost.signature.client.archive;

import no.digipost.signature.client.core.ResponseInputStream;
import no.digipost.signature.client.core.WithSignatureServiceRootUrl;
import no.digipost.signature.client.core.internal.DownloadHelper;
import no.digipost.signature.client.core.internal.http.SignatureServiceRoot;
import org.apache.hc.client5.http.classic.HttpClient;

public class ArchiveClient {

    public interface Configuration extends WithSignatureServiceRootUrl {
        HttpClient httpClientForDocumentDownloads();
    }

    private DownloadHelper download;

    public ArchiveClient(Configuration configuration) {
        this.download = new DownloadHelper(SignatureServiceRoot.from(configuration), configuration.httpClientForDocumentDownloads());
    }

    public ResponseInputStream getPAdES(ArchiveOwner owner, String id) {
        return download.getDataStream(owner.getOrganizationNumber() + "/archive/documents/" + id + "/pades");
    }

}
