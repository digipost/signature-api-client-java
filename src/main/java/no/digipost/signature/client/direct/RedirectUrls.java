package no.digipost.signature.client.direct;

import no.digipost.signature.api.xml.XMLSignerSpecificUrl;

import java.util.List;

public class RedirectUrls {

    private List<RedirectUrl> urls;

    RedirectUrls(List<RedirectUrl> urls) {
        this.urls = urls;
    }

    String getSingleRedirectUrl() {
        if (urls.size() != 1) {
            throw new IllegalStateException("Calls to this method should only be done when there are no more than one (1) redirect URL.");
        }
        return urls.get(0).getUrl();
    }

    /**
     * Gets the redirect URL for a given signer.
     * @throws IllegalArgumentException if the job response doesn't contain a redirect URL for this signer
     * @see DirectJobResponse#getSingleRedirectUrl()
     */
    public String getFor(String personalIdentificationNumber) {
        for (RedirectUrl redirectUrl : urls) {
            if (redirectUrl.signer.equals(personalIdentificationNumber)) {
                return redirectUrl.url;
            }
        }
        throw new IllegalArgumentException("Unable to find redirect URL for this signer");
    }

    public List<RedirectUrl> getAll() {
        return urls;
    }

    public static class RedirectUrl {

        private final String signer;

        private final String url;

        private RedirectUrl(String signer, String url) {
            this.signer = signer;
            this.url = url;
        }

        static RedirectUrl fromJaxb(XMLSignerSpecificUrl xmlSignerSpecificUrl) {
            return new RedirectUrl(xmlSignerSpecificUrl.getSigner(), xmlSignerSpecificUrl.getValue());
        }

        public String getSigner() {
            return signer;
        }

        public String getUrl() {
            return url;
        }

    }

}
