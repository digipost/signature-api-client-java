---
identifier: directusecases
title: Direct use cases
layout: default
---

<h3 id="uc01">Create client configuration</h3>


``` java 

InputStream keyStore = null; // Stream created from keyStore file

KeyStoreConfig keyStoreConfig = KeyStoreConfig.fromKeyStore(keyStore,
        "certificateAlias", "keyStorePassword", "privateKeyPassword");

ClientConfiguration clientConfiguration = ClientConfiguration.builder(keyStoreConfig)
        .globalSender(new Sender("123456789"))
        .build();

```

> Note: For organizations acting as *brokers* on behalf of multiple *senders*, you may specify the sender's organization number on each signature job. The sender specified for a job will always take precedence over the `globalSender` in `ClientConfiguration`

<h3 id="uc02">Create and send signature job</h3>

``` java

ClientConfiguration clientConfiguration = null; // As initialized earlier
DirectClient client = new DirectClient(clientConfiguration);

byte[] documentBytes = null; // Loaded document bytes
DirectDocument document = DirectDocument.builder("Subject", "document.pdf", documentBytes).build();

ExitUrls exitUrls = ExitUrls.of(
        "http://sender.org/onCompletion",
        "http://sender.org/onRejection",
        "http://sender.org/onError"
);

DirectSigner signer = DirectSigner.withPersonalIdentificationNumber("12345678910").build();
DirectJob directJob = DirectJob.builder(document, exitUrls, signer).build();

DirectJobResponse directJobResponse = client.create(directJob);

```

> Note: Most domain object follow the builder pattern, accepting all required parameters in the factory method and with specific methods for each optional parameter. Keep this in mind when exploring the API.

<h3 id="uc03">Get signature job status</h3>

The signing process is a synchrounous operation in the direct use case. There is no need to poll for changes to a signature job, as the status is well known to the sender of the job. As soon as the signer completes, rejects or an error occurs, the user is redirected to the respective URLs set in `ExitUrls`. A `status_query_token` parameter has been added to the url, use this when requesting a status change.

``` java

DirectClient client = null; // As initialized earlier
DirectJobResponse directJobResponse = null; // As returned when creating signature job

String statusQueryToken = "0A3BQ54C…";

DirectJobStatusResponse directJobStatusResponse =
        client.getStatus(StatusReference.of(directJobResponse).withStatusQueryToken(statusQueryToken));

```

<h3 id="uc4">Create job and get status by polling</h3> 

If you, for any reason, are unable to retrieve status by using the status query token specified above, you may poll the service for any changes done to your organization’s jobs. If the queue is empty, additional polling will give an exception.

<blockquote>Note: For the job to be available in the polling queue, make sure to specify the job's <code>StatusRetrievalMethod</code> as illustrated below.</blockquote>

``` java

DirectClient client = null; // As initialized earlier

DirectJob directJob = DirectJob.builder(document, exitUrls, signer)
        .retrieveStatusBy(StatusRetrievalMethod.POLLING)
        .build();

client.create(directJob);

DirectJobStatusResponse statusChange = client.getStatusChange();

if (statusChange.is(DirectJobStatus.NO_CHANGES)) {
    // Queue is empty. Must wait before polling again
} else {
    // Received status update, act according to status
    DirectJobStatus status = statusChange.getStatus();
}

client.confirm(statusChange);

```

<h3 id="uc05">Get signed documents</h3>

``` java

DirectClient client = null; // As initialized earlier
DirectJobStatusResponse directJobStatusResponse = null; // As returned when getting job status

if (directJobStatusResponse.isPAdESAvailable()) {
    InputStream pAdESStream = client.getPAdES(directJobStatusResponse.getpAdESUrl());
}

for (Signature signature : directJobStatusResponse.getSignatures()) {
    if (signature.is(SignerStatus.SIGNED)) {
        InputStream xAdESStream = client.getXAdES(signature.getxAdESUrl());
    }
}

```

<h3 id="uc06">Confirm processed signature job</h3>

``` java
DirectClient client = null; // As initialized earlier
DirectJobStatusResponse directJobStatusResponse = null; // As returned when getting job status

client.confirm(directJobStatusResponse);

```


[comment]: <> (Using h3 with specific id to diff from the auto genereted one for portal use cases.)

<h3 id="crete-client-configuration-direct">Specifying queues</h3>

Specifies the queue that jobs and status changes for a signature job will occur in for signature jobs where `StatusRetrievalMethod == POLLING` This is a feature aimed at organizations where it makes sense to retrieve status changes from several queues. This may be if the organization has more than one division, and each division has an application that create signature jobs through the API and want to retrieve status changes independent of the other division's actions.

To specify a queue, set `Sender.pollingQueue` through the constructor `Sender(String, PollingQueue)`. Please note that the same sender must be specified when polling to retrieve status changes. The `Sender` can be set globally in `ClientConfiguration` or on every job.

``` java 

DirectClient client = null; // As initialized earlier
Sender sender = new Sender("000000000", PollingQueue.of("CustomPollingQueue"));

DirectJob directJob = DirectJob.builder(document, exitUrls, signer)
      .retrieveStatusBy(StatusRetrievalMethod.POLLING).withSender(sender)
      .build();

client.create(directJob);

DirectJobStatusResponse statusChange = client.getStatusChange(sender);

if (statusChange.is(DirectJobStatus.NO_CHANGES)) {
  // Queue is empty. Must wait before polling again
} else {
  // Recieved status update, act according to status
  DirectJobStatus status = statusChange.getStatus();
}

client.confirm(statusChange);

```
