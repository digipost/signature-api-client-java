---
identifier: directusecases
title: Direct use cases
layout: default
---

<h3 id="uc01">Create client configuration</h3>

{% highlight java %}

KeyStoreConfig keyStoreConfig = KeyStoreConfig.fromKeyStore(keyStore,
        certificateAlias, keyStorePassword, privateKeyPassword);

ClientConfiguration clientConfiguration = ClientConfiguration.builder(keyStoreConfig)
        .globalSender(new Sender("123456789"))
        .build();

{% endhighlight %}

> Note: For organizations acting as *brokers* on behalf of multiple *senders*, you may specify the sender's organization number on each signature job. The sender specified for a job will always take precedence over the `globalSender` in `ClientConfiguration`

<h3 id="uc02">Create and send signature job</h3>

{% highlight java %}

ClientConfiguration clientConfiguration = ...; // As initialized earlier
DirectClient client = new DirectClient(clientConfiguration);

byte[] documentBytes = ...;
DirectDocument document = DirectDocument.builder("Subject", "document.pdf", documentBytes).build();

ExitUrls exitUrls = ExitUrls.of(
        "http://sender.org/onCompletion",
        "http://sender.org/onRejection",
        "http://sender.org/onError"
);

DirectSigner signer = DirectSigner.withPersonalIdentificationNumber("12345678910").build();
DirectJob directJob = DirectJob.builder(document, exitUrls, signer).build();

DirectJobResponse directJobResponse = client.create(directJob);

{% endhighlight %}

> Note: Most domain object follow the builder pattern, accepting all required parameters in the factory method and with specific methods for each optional parameter. Keep this in mind when exploring the API.

<h3 id="uc03">Get signature job status</h3>

The signing process is a synchrounous operation in the direct use case. There is no need to poll for changes to a signature job, as the status is well known to the sender of the job. As soon as the signer completes, rejects or an error occurs, the user is redirected to the respective URLs set in `ExitUrls`. A `status_query_token` parameter has been added to the url, use this when requesting a status change.

{% highlight java %}

DirectClient client = ...; // As initialized earlier
DirectJobResponse directJobResponse = ...; // As returned when creating signature job

String statusQueryToken = "0A3BQ54C…";

DirectJobStatusResponse directJobStatusResponse =
        client.getStatus(StatusReference.of(directJobResponse).withStatusQueryToken(statusQueryToken));

{% endhighlight %}

<h3 id="uc4">Get direct job status by polling</h3> 

If you, for any reason, are unable to retrieve status by using the status query token specified above, you may poll the service for any changes done to your organization’s jobs. If the queue is empty, additional polling will give an exception.

<blockquote>Note: For the job to be available in the polling queue, make sure to specify the job's <code>StatusRetrievalMethod</code> as illustrated below.</blockquote>

{% highlight java %}
DirectClient client = ...; // As initialized earlier

DirectJob directJob = DirectJob.builder(document, exitUrls, signer)
        .retrieveStatusBy(StatusRetrievalMethod.POLLING)
        .build();

client.create(directJob);

DirectJobStatusResponse statusChange = client.getStatusChange();

if (statusChange.is(DirectJobStatus.NO_CHANGES)) {
    // Queue is empty. Must wait before polling again
} else {
    // Recieved status update, act according to status
    DirectJobStatus status = statusChange.getStatus();
}

client.confirm(statusChange);

{% endhighlight %}

<h3 id="uc05">Get signed documents</h3>

{% highlight java %}

DirectClient client = ...; // As initialized earlier
DirectJobStatusResponse directJobStatusResponse = ...; // As returned when getting job status

if (directJobStatusResponse.isPAdESAvailable()) {
    InputStream pAdESStream = client.getPAdES(directJobStatusResponse.getpAdESUrl());
}

for (Signature signature : directJobStatusResponse.getSignatures()) {
    if (signature.is(SignerStatus.SIGNED)) {
        InputStream xAdESStream = client.getXAdES(signature.getxAdESUrl());
    }
}

{% endhighlight %}

<h3 id="uc06">Confirm processed signature job</h3>

{% highlight java %}

DirectClient client = ...; // As initialized earlier
DirectJobStatusResponse directJobStatusResponse = ...; // As returned when getting job status

client.confirm(directJobStatusResponse);

{% endhighlight %}