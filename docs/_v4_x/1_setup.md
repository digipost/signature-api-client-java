---
identifier: setup
title: Initial setup
layout: default
---

The client library is available on [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22no.digipost.signature%22%20AND%20a%3A%22signature-api-client-java%22). To use the library in your application, add the following dependency to your `pom.xml`:

{% highlight xml %}
<dependency>
    <groupId>no.digipost.signature</groupId>
    <artifactId>signature-api-client-java</artifactId>
    <version>4.0</version>
</dependency>
{% endhighlight %}

#### Javadoc

Javadoc is available here: [javadoc.io/doc/no.digipost.signature/signature-api-client-java](https://www.javadoc.io/doc/no.digipost.signature/signature-api-client-java/4.0)

### Create client configuration
A client configuration includes all organization specific configuration and all settings needed to connect to the correct environment for Posten signering.


#### Load organization certificate

The first step is to load the organization certificate (virksomhetssertifikat) through the `KeyStoreConfig`. It can be created from a Java Key Store (JKS) or directly from a PKCS12-container, which is the usual format of an organization certificate. The latter is the recommended way of loading it if you have the certificate stored as a simple file:


``` java
KeyStoreConfig keyStoreConfig;
try (InputStream certificateStream = Files.newInputStream(Paths.get("/path/to/certificate.p12"))) {
    keyStoreConfig = KeyStoreConfig.fromOrganizationCertificate(
            certificateStream, "CertificatePassword"
    );
}
```

If you have a Java Key Store file containing the organization certificate, it can be loaded in the following way:

``` java
KeyStoreConfig keyStoreConfig;
try (InputStream certificateStream = Files.newInputStream(Paths.get("/path/to/javakeystore.jks"))) {
    keyStoreConfig = KeyStoreConfig.fromJavaKeyStore(
            certificateStream,
            "OrganizationCertificateAlias",
            "KeyStorePassword",
            "CertificatePassword"
    );
}
```

When the certificate has been loaded correctly, a `ClientConfiguration` can be initialized. A _trust store_ and _service Uri_ needs to be set to properly connect. Please change the trust store and service Uri in the following example when connecting to our production environment.  

``` java 
KeyStoreConfig keyStoreConfig = null; //As initialized earlier

ClientConfiguration clientConfiguration = ClientConfiguration.builder(keyStoreConfig)
        .trustStore(Certificates.TEST)
        .serviceUri(ServiceUri.DIFI_TEST)
        .globalSender(new Sender("123456789"))
        .build();

``` 

> Note: For organizations acting as *brokers* on behalf of multiple *senders*, you may specify the sender's organization number on each signature job. The sender specified for a job will always take precedence over the `globalSender` in `ClientConfiguration`
