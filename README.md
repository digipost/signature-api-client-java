# Posten Signering – Java client Library
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/no.digipost.signature/signature-api-client-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/no.digipost.signature/signature-api-client-java)
[![javadoc](https://javadoc.io/badge2/no.digipost.signature/signature-api-client-java/javadoc.svg?logo=java&color=yellow)](https://javadoc.io/doc/no.digipost.signature/signature-api-client-java)
![](https://github.com/digipost/signature-api-client-java/workflows/Build%20and%20deploy/badge.svg)
[![License](https://img.shields.io/badge/license-Apache%202-blue)](https://github.com/digipost/signature-api-client-java/blob/main/LICENCE)

This repo is the Java client library for integrating with **Posten signering**.

If you are looking for the C# variant of this library, please see [signature-api-client-dotnet](https://github.com/digipost/signature-api-client-dotnet).

## Documentation

Get started by [reading the documentation](http://signering-docs.rtfd.io/).


## Dependency

The recommended way to declare dependency on the library is to utilize the [BOM](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#bill-of-materials-bom-poms). With Maven, this is declared like this:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>no.digipost.signature</groupId>
            <artifactId>signature-api-client-bom</artifactId>
            <version>7.0.4</version> <!-- replace with any later version -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
...
```

And depend directly on the library artifact (without any specified version, since this is supplied by the BOM artifact above)
```xml
<dependencies>
    <dependency>
        <groupId>no.digipost.signature</groupId>
        <artifactId>signature-api-client-java</artifactId>
    </dependency>
...
```


## Upgrading

> [!IMPORTANT]
> Clients are always strongly encouraged to upgrade to the [latest][latest-release] version.

The _minimal_ version which is currently supported is [v6.1][minimum-seid2-release]. Any earlier release is expected to stop working after January 7th 2025 due to Posten signering transitioning from SEIDv1 to the SEIDv2 certificate standard in the API. (Note: SEIDv2 used for clients' certificates is not affected, and has been supported for several years already. No action is required for client certificates.)




### Changes v4.6.x ➡️ v6.1.x


- change all `Document.FileType` references to `DocumentType`
- multiple documents in a signature job is now supported, and both [PortalJob.builder(..)](https://javadoc.io/static/no.digipost.signature/signature-api-client-java/6.1.1/no/digipost/signature/client/portal/PortalJob.html#builder(java.lang.String,java.util.List,java.util.List)) and [DirectJob.builder(..)](https://javadoc.io/static/no.digipost.signature/signature-api-client-java/6.1.1/no/digipost/signature/client/direct/DirectJob.html#builder(java.lang.String,java.util.List,java.util.List,no.digipost.signature.client.direct.WithExitUrls)) methods are changed according to this:
    - the **title of the job** is the first parameter (this was previously set on the document itself)
    - the second and third parameter are either **lists of documents and signers**, or a single document and a single signer
    - for direct jobs, **exit-URLs** are provided as the fourth parameter
    - the optional description for the job (text with more details displayed for the signer) is set on the builder instance: [.withDescription(String)](https://javadoc.io/static/no.digipost.signature/signature-api-client-java/6.1.1/no/digipost/signature/client/direct/DirectJob.Builder.html#withDescription(java.lang.String))
- availability for portal jobs:
    - The [PortalJob.Builder.availableFor(..)](https://javadoc.io/static/no.digipost.signature/signature-api-client-java/6.1.1/no/digipost/signature/client/portal/PortalJob.Builder.html#availableFor(java.time.Duration)) now accepts a [Duration][java.time.Duration] instead of separate `long` and `TimeUnit` arguments.
- response from `DirectClient.create(DirectJob)`
    - `RedirectUrls` is replaced with [getting a list of DirectSigners](https://javadoc.io/static/no.digipost.signature/signature-api-client-java/6.1.1/no/digipost/signature/client/direct/DirectJobResponse.html#getSigners()) from where the redirect-URL for each signer can be obtained.
    - In the case of jobs with a single signer, it was previously possible to get the signer's redirect-URL from `DirectJobResponse.getSingleRedirectUrl()`. This is now changed to `DirectJobResponse.getSingleSigner()` and you can [get the redirect URL](https://javadoc.io/static/no.digipost.signature/signature-api-client-java/6.1.1/no/digipost/signature/client/direct/DirectSignerResponse.html#getRedirectUrl()) from the returned single `DirectSigner` instance. The URL is represented as a [URI][java.net.URI] instead of previously being a `String`.


### Changes v6.1.x ➡️ v7.x

If you are upgrading from an earlier version to v7.x, see also applicable previous section(s).

- see the [Dependency](#dependency) section for how to properly declare your dependency using the BOM
- see release notes for [v7.0.1][jakarta-support-release] for more details. Only the [configuration API](https://javadoc.io/static/no.digipost.signature/signature-api-client-java/7.0.4/no/digipost/signature/client/ClientConfiguration.Builder.html) has got some minor breaking changes:
    - "globalSender" is renamed to [.defaultSender(Sender)](https://javadoc.io/static/no.digipost.signature/signature-api-client-java/7.0.4/no/digipost/signature/client/ClientConfiguration.Builder.html#defaultSender(no.digipost.signature.client.core.Sender))
    - "serviceUri" and "trustStore" has been combined and replaced with [ServiceEnvironment](https://javadoc.io/static/no.digipost.signature/signature-api-client-java/7.0.4/no/digipost/signature/client/ServiceEnvironment.html). Instead of separately specifying the former two, instead specify either `ServiceEnvironment.STAGING` or `ServiceEnvironment.PRODUCTION` depending on which environment you intend to use.




[minimum-seid2-release]: https://github.com/digipost/signature-api-client-java/releases/6.1
[jakarta-support-release]: https://github.com/digipost/signature-api-client-java/releases/7.0.1
[latest-release]: https://github.com/digipost/signature-api-client-java/releases/latest
[java.net.URI]: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URI.html
[java.time.Duration]: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Duration.html
