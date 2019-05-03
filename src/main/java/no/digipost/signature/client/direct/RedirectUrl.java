package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLSignerSpecificUrl;

public class RedirectUrl {

    static RedirectUrl fromJaxb(XMLSignerSpecificUrl xmlSignerSpecificUrl) {
        return new RedirectUrl(xmlSignerSpecificUrl.getSigner(), xmlSignerSpecificUrl.getValue());
    }


    private final String signer;

    private final String url;

    private RedirectUrl(String signer, String url) {
        this.signer = signer;
        this.url = url;
    }

    public String getSigner() {
        return signer;
    }

    public String getUrl() {
        return url;
    }

}
