package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLSignerSpecificUrl;

import java.net.URI;

public class RedirectUrl {

    static RedirectUrl fromJaxb(XMLSignerSpecificUrl xmlSignerSpecificUrl) {
        return new RedirectUrl(xmlSignerSpecificUrl.getSigner(), xmlSignerSpecificUrl.getValue());
    }


    private final String signer;

    private final URI url;

    private RedirectUrl(String signer, URI url) {
        this.signer = signer;
        this.url = url;
    }

    public String getSigner() {
        return signer;
    }

    public URI getUrl() {
        return url;
    }

}
