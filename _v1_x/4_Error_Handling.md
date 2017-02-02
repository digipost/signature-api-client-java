---
identifier: errorhandling
title: Error Handling
layout: default
---

<h3 id="errorHandlerHeader">Handling errors</h3>

Different forms of exceptions may occur, some are more specific than others. All exceptions related to client behavior inherits from `SignatureException`. 

{% highlight java %}

try {
    client.confirm(statusChange);
} catch (BrokerNotAuthorizedException brokerNotAuthorized) {
    // Broker is not authorized to perform action. Contact Difi in order to set up access rights.
} catch (UnexpectedResponseException unexpectedResponse) {
    // The server returned an unexpected response.
    Response.Status httpStatusCode = unexpectedResponse.getActualStatus();

    // errorCode and errorMesage will normally contain information returned by the server. May be null.
    String errorCode = unexpectedResponse.getErrorCode();
    String errorMessage = unexpectedResponse.getErrorMessage();
} catch (SignatureException e) {
    // An unexpected exception was thrown, inspect e.getMessage().
}

{% endhighlight %}