---
identifier: portalusecases
title: Portal use cases
layout: default
---

### Create Client Configuration

``` java

InputStream keyStore = null; // Stream created from keyStore file

KeyStoreConfig keyStoreConfig = KeyStoreConfig.fromKeyStore(keyStore,
        "certificateAlias", "keyStorePassword", "privateKeyPassword");

ClientConfiguration clientConfiguration = ClientConfiguration.builder(keyStoreConfig)
        .globalSender(new Sender("123456789"))
        .build();

```

> Note: For organizations acting as *brokers* on behalf of multiple *senders*, you may specify the sender's organization number on each signature job. The sender specified for a job will always take precedence over the `globalSender` in `ClientConfiguration`

### Create and send signature job

The following example shows how to create a document and send it to two signers.

``` java

ClientConfiguration clientConfiguration = null; // As initialized earlier
PortalClient client = new PortalClient(clientConfiguration);

byte[] documentBytes = null; // Loaded document bytes
PortalDocument document = PortalDocument.builder("Subject", "document.pdf", documentBytes).build();

PortalJob portalJob = PortalJob.builder(
        document,
        PortalSigner.identifiedByPersonalIdentificationNumber("12345678910",
                NotificationsUsingLookup.EMAIL_ONLY).build(),
        PortalSigner.identifiedByPersonalIdentificationNumber("12345678911",
                Notifications.builder().withEmailTo("email@example.com").build()).build(),
        PortalSigner.identifiedByEmail("email@example.com").build()
).build();

PortalJobResponse portalJobResponse = client.create(portalJob);

```

You may identify the signature job's signers by personal identification number (`identifiedByPersonalIdentificationNumber(…)`) or contact information. When identifying by contact information, you may choose between instantiating a `PortalSigner` using `identifiedByEmail(…)`, `identifiedByMobileNumber(…)` or `identifiedByEmailAndMobileNumber(…, …)`.

Read more about identifying your signers in the [functional documentation](http://digipost.github.io/signature-api-specification/v1.0/#kontaktinfo) (Norwegian).

> Note: Most domain object follow the builder pattern, accepting all required parameters in the factory method and with specific methods for each optional parameter. Keep this in mind when exploring the API.

### Get status changes

All changes to signature jobs will be added to a queue from which you can poll for status updates. If the queue is empty (i.e. no jobs have changed status since last poll), you are not allowed to poll again for a defined period. Refer to the [API specification](https://github.com/digipost/signature-api-specification/blob/master/README.md#hvor-ofte-skal-du-polle) to see how long this period is.

The following example shows how this can be handled:

``` java

PortalClient client = null; // As initialized earlier

PortalJobStatusChanged statusChange = client.getStatusChange();

if (statusChange.is(PortalJobStatus.NO_CHANGES)) {
    // Queue is empty. Must wait before polling again
} else {
    // Recieved status update, act according to status
    PortalJobStatus signatureJobStatus = statusChange.getStatus();
}

// Polling immediately after retrieving NO_CHANGES:
try {
    client.getStatusChange();
} catch (TooEagerPollingException tooEagerPolling) {
    Instant nextPermittedPollTime = tooEagerPolling.getNextPermittedPollTime();
}

```

Retrieve a specific signer's status by calling the method `statusChange.getSignatureFrom(SignerIdentifier)`. The `SignerIdentifier` parameter must be created by using [one of the static method](https://javadoc.io/page/no.digipost.signature/signature-api-client-java/latest/no/digipost/signature/client/portal/SignerIdentifier.html) corresponding with the method you used when creating the signature job.

``` java

PortalJobStatusChanged statusChange = null; // As initialized earlier

Signature signature = statusChange.getSignatureFrom(
        SignerIdentifier.identifiedByPersonalIdentificationNumber("12345678910")
);

```

### Get signed documents

When getting XAdES and PAdES for a `PortalJob`, remember that the XAdES is per signer, while there is only one PAdES. 

``` java

PortalClient client = null; // As initialized earlier
PortalJobStatusChanged statusChange = null; // As returned when polling for status changes

// Retrieve PAdES:
if (statusChange.isPAdESAvailable()) {
    InputStream pAdESStream = client.getPAdES(statusChange.getpAdESUrl());
}

// Retrieve XAdES for all signers:
for (Signature signature : statusChange.getSignatures()) {
    if (signature.is(SignatureStatus.SIGNED)) {
        InputStream xAdESStream = client.getXAdES(signature.getxAdESUrl());
    }
}

// … or for one specific signer:
Signature signature = statusChange.getSignatureFrom(
        SignerIdentifier.identifiedByPersonalIdentificationNumber("12345678910"));
if (signature.is(SignatureStatus.SIGNED)) {
    InputStream xAdESStream = client.getXAdES(signature.getxAdESUrl());
}

```

### Confirm processed signature job

To avoid this status change to return to the queue, you must confirm that it has been processed.

``` java

PortalClient client = ...; // As initialized earlier
PortalJobStatusChanged statusChange = ...; // As returned when polling for status changes

client.confirm(statusChange);

```

### Specifying queues

Specifies the queue that jobs and status changes for a signature job will occur in. This is a feature aimed at organizations where it makes sense to retrieve status changes from several queues. This may be if the organization has more than one division, and each division has an application that create signature jobs through the API and want to retrieve status changes independent of the other division's actions.

To specify a queue, set `Sender.pollingQueue` through the constructor `Sender(String, PollingQueue)`. Please note that the same sender must be specified when polling to retrieve status changes. The `Sender` can be set globally in `ClientConfiguration` or on every job.

``` java 

ClientConfiguration clientConfiguration = null; // As initialized earlier
PortalClient client = new PortalClient(clientConfiguration);

Sender sender = new Sender("000000000", PollingQueue.of("CustomPollingQueue"));

byte[] documentBytes = null; // Loaded document bytes
PortalDocument document = PortalDocument.builder("Subject", "document.pdf", documentBytes).build();

PortalJob portalJob = PortalJob.builder(
        document,
        PortalSigner.identifiedByPersonalIdentificationNumber("12345678910",
                NotificationsUsingLookup.EMAIL_ONLY).build(),
        PortalSigner.identifiedByPersonalIdentificationNumber("12345678911",
                Notifications.builder().withEmailTo("email@example.com").build()).build(),
        PortalSigner.identifiedByEmail("email@example.com").build()
).withSender(sender).build();

PortalJobResponse portalJobResponse = client.create(portalJob);

PortalJobStatusChanged statusChange = client.getStatusChange(sender);

```
