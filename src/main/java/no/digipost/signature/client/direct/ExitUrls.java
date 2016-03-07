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

public final class ExitUrls implements WithExitUrls {

    /**
     * A single exit url can be used if you do not need to separate
     * resources for handling the different outcomes of a direct job.
     * This is simply a convenience factory method for
     * {@link ExitUrls#of(String, String, String)} with the same url
     * given for all the arguments.
     *
     * @param url The url you want the user to be redirected to upon
     *        completing the signing ceremony, regardless of its outcome
     */
    public static ExitUrls singleExitUrl(String url) {
        return of(url, url, url);
    }

    /**
     * Specify the urls the user is will be redirected to for different
     * outcomes of a signing ceremony. When the user is redirected, the urls will
     * have an appended query parameter ({@link StatusReference#STATUS_QUERY_TOKEN_PARAM_NAME})
     * which contains a token required to {@link DirectClient#getStatus(StatusReference) query for the status of the job}.
     *
     * @param completionUrl the user will be redirected to this url after having successfully signed the document.
     * @param rejectionUrl the user will be redirected to this url if actively rejecting to sign the document.
     * @param errorUrl the user will be redirected to this url if any unexpected error happens during the signing ceremony.
     */
    public static ExitUrls of(String completionUrl, String rejectionUrl, String errorUrl) {
        return new ExitUrls(completionUrl, rejectionUrl, errorUrl);
    }

    private final String completionUrl;
    private final String rejectionUrl;
    private final String errorUrl;

    private ExitUrls(String completionUrl, String rejectionUrl, String errorUrl) {
        this.completionUrl = completionUrl;
        this.rejectionUrl = rejectionUrl;
        this.errorUrl = errorUrl;
    }

    @Override
    public String getCompletionUrl() {
        return completionUrl;
    }

    @Override
    public String getRejectionUrl() {
        return rejectionUrl;
    }

    @Override
    public String getErrorUrl() {
        return errorUrl;
    }
}
