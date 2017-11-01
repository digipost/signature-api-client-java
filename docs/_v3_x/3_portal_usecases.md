---
identifier: portalusecases
title: Portal use cases
layout: default
---

<h3 id="uc07">Create Client Configuration</h3>

{% highlight java %}

KeyStoreConfig keyStoreConfig = KeyStoreConfig.fromKeyStore(keyStore,
        certificateAlias, keyStorePassword, privateKeyPassword);

ClientConfiguration clientConfiguration = ClientConfiguration.builder(keyStoreConfig)
        .globalSender(new Sender("123456789"))
        .build();

{% endhighlight %}

> Note: For organizations acting as *brokers* on behalf of multiple *senders*, you may specify the sender's organization number on each signature job. The sender specified for a job will always take precedence over the `globalSender` in `ClientConfiguration`

<h3 id="uc08">Create and send signature job</h3>

The following example shows how to create a document and send it to two signers.

{% highlight java %}

ClientConfiguration clientConfiguration = ...; // As initialized earlier
PortalClient client = new PortalClient(clientConfiguration);

byte[] documentBytes = ...;
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

{% endhighlight %}

You may identify the signature job's signers by personal identification number (`identifiedByPersonalIdentificationNumber(…)`) or contact information. When identifying by contact information, you may choose between instantiating a `PortalSigner` using `identifiedByEmail(…)`, `identifiedByMobileNumber(…)` or `identifiedByEmailAndMobileNumber(…, …)`.

Read more about identifying your signers in the [functional documentation](http://digipost.github.io/signature-api-specification/v1.0/#kontaktinfo) (Norwegian).

> Note: Most domain object follow the builder pattern, accepting all required parameters in the factory method and with specific methods for each optional parameter. Keep this in mind when exploring the API.

<h3 id="uc09">Get status changes</h3>

All changes to signature jobs will be added to a queue from which you can poll for status updates. If the queue is empty (i.e. no jobs have changed status since last poll), you are not allowed to poll again for a defined period. Refer to the [API specification](https://github.com/digipost/signature-api-specification/blob/master/README.md#hvor-ofte-skal-du-polle) to see how long this period is.

The following example shows how this can be handled:

{% highlight java %}

PortalClient client = ...; // As initialized earlier

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

{% endhighlight %}

Retrieve a specific signer's status by calling the method `statusChange.getSignatureFrom(SignerIdentifier)`. The `SignerIdentifier` parameter must be created by using [one of the static method](https://javadoc.io/page/no.digipost.signature/signature-api-client-java/latest/no/digipost/signature/client/portal/SignerIdentifier.html) corresponding with the method you used when creating the signature job.

{% highlight java %}

Signature signature = statusChange.getSignatureFrom(
    SignerIdentifier.identifiedByPersonalIdentificationNumber("12345678910"));

{% endhighlight %}

<h3 id="uc10">Get signed documents</h3>

When getting XAdES and PAdES for a `PortalJob`, remember that the XAdES is per signer, while there is only one PAdES. 

{% highlight java %}

PortalClient client = ...; // As initialized earlier
PortalJobStatusChanged statusChange = ...; // As returned when polling for status changes

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

{% endhighlight %}

<h3 id="uc11">Confirm processed signature job</h3>

To avoid this status change to return to the queue, you must confirm that it has been processed.

{% highlight java %}

PortalClient client = ...; // As initialized earlier
PortalJobStatusChanged statusChange = ...; // As returned when polling for status changes

client.confirm(statusChange);

{% endhighlight %}
