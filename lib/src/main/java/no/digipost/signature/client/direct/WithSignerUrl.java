package no.digipost.signature.client.direct;

import java.net.URI;

@FunctionalInterface
public interface WithSignerUrl {

    static JustSignerUrl of(URI url) {
        return new JustSignerUrl(url);
    }

    final class JustSignerUrl implements WithSignerUrl {
        private final URI signerUrl;

        private JustSignerUrl(URI url) {
            this.signerUrl = url;
        }

        @Override
        public URI getSignerUrl() {
            return signerUrl;
        }
    }

    /**
     * @return the URL for a specific signer of a signature job
     */
    URI getSignerUrl();

}
