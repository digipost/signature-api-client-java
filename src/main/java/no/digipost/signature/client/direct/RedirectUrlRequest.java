package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLSignerSpecificUrl;

public final class RedirectUrlRequest {

    static RedirectUrlRequest fromJaxb(XMLSignerSpecificUrl xmlSignerSpecificUrl) {
        return new RedirectUrlRequest(xmlSignerSpecificUrl.getSigner(), xmlSignerSpecificUrl.getValue());
    }

    public final String url;

    public final String signer;

    public RedirectUrlRequest(String signer, String url) {
        this.url = url;
        this.signer = signer;
    }

}
