/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.signature.client.direct;

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

        public RedirectUrl(String signer, String url) {
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

}
