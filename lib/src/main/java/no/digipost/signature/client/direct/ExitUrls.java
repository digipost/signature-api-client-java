package no.digipost.signature.client.direct;

import java.net.URI;

public final class ExitUrls implements WithExitUrls {

    /**
     * A single exit url can be used if you do not need to separate
     * resources for handling the different outcomes of a direct job.
     * This is simply a convenience factory method for
     * {@link ExitUrls#of(URI, URI, URI)} with the same url
     * given for all the arguments.
     *
     * @param url The url you want the user to be redirected to upon
     *        completing the signing ceremony, regardless of its outcome
     */
    public static ExitUrls singleExitUrl(URI url) {
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
    public static ExitUrls of(URI completionUrl, URI rejectionUrl, URI errorUrl) {
        return new ExitUrls(completionUrl, rejectionUrl, errorUrl);
    }

    private final URI completionUrl;
    private final URI rejectionUrl;
    private final URI errorUrl;

    private ExitUrls(URI completionUrl, URI rejectionUrl, URI errorUrl) {
        this.completionUrl = completionUrl;
        this.rejectionUrl = rejectionUrl;
        this.errorUrl = errorUrl;
    }

    @Override
    public URI getCompletionUrl() {
        return completionUrl;
    }

    @Override
    public URI getRejectionUrl() {
        return rejectionUrl;
    }

    @Override
    public URI getErrorUrl() {
        return errorUrl;
    }
}
