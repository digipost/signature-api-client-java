---
id: portalusecases
title: Portal use cases
layout: default
---

<h3 id="uc06">Create Client Configuration</h3>

{% highlight java %}

KeyStoreConfig keystoreConfig = KeyStoreConfig.fromKeyStore(keyStore,
        certificateAlias, keyStorePassword, privateKeyPassword);

ClientConfiguration clientConfiguration = ClientConfiguration.builder(keyStoreConfig)
        .globalSender(new Sender("123456789"))
        .build();

{% endhighlight %}

> Note: For organizations acting as *brokers* on behalf of multiple *senders*, you may specify the sender's organization number on each signature job. The sender specified for a job will always take precedence over the `globalSender` in `ClientConfiguration`

<h3 id="uc07">Create and send signature job</h3>

The following example shows how to create a document and send it to two signers.

{% highlight java %}

ClientConfiguration clientConfiguration = ...; // As initialized earlier
PortalClient client = new PortalClient(clientConfiguration);

byte[] documentBytes = ...;
Document document = Document.builder("Subject", "document.pdf", documentBytes).build();

PortalJob portalJob =
        PortalJob.builder(document, new Signer("12345678910"), new Signer("12345678911")).build();

PortalJobResponse portalJobResponse = client.create(portalJob);

{% endhighlight %}


<h3 id="uc08">Get status changes</h3>

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
    Date nextPermittedPollTime = tooEagerPolling.getNextPermittedPollTime();
}

{% endhighlight %}

<h3 id="uc09">Get signed documents</h3>

When getting XAdES and PAdES for a `PortalJob`, remember that the XAdES is per signer, while there is only one PAdES. 

{% highlight java %}

PortalClient client = ...; // As initialized earlier
PortalJobStatusChanged statusChange = ...; // As returned when polling for status changes

if (statusChange.isPAdESAvailable()) {
    InputStream pAdESStream = client.getPAdES(statusChange.getpAdESUrl());
}

for (Signature signature : statusChange.getSignatures()) {
    if (signature.is(SignatureStatus.SIGNED)) {
        InputStream xAdESStream = client.getXAdES(signature.getxAdESUrl());
    }
}

{% endhighlight %}

<h3 id="uc10">Confirm processed signature job</h3>

To avoid this status change to return to the queue, you must confirm that it has been processed.

{% highlight java %}

PortalClient client = ...; // As initialized earlier
PortalJobStatusChanged statusChange = ...; // As returned when polling for status changes

client.confirm(statusChange);

{% endhighlight %}