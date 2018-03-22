# Posten Signering – Java Klientbibliotek

Dette repoet inneholder klientbibliotek implementert i Java som kan benyttes for å integrere mot Posten Signering.

Nærmere dokumentasjon av APIet og detaljer rundt bruk kan finnes i repoet [Signature-API-Specification](https://github.com/digipost/signature-api-specification)



---


## Bruke biblioteket


### Maven-avhengighet

Klientbiblioteket er releaset til [![Maven Central](https://maven-badges.herokuapp.com/maven-central/no.digipost.signature/signature-api-client-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/no.digipost.signature/signature-api-client-java). For å avhenge av biblioteket, legg til følgende i `pom.xml`:

```xml
<dependency>
    <groupId>no.digipost.signature</groupId>
    <artifactId>signature-api-client-java</artifactId>
    <version>4.0</version>
</dependency>
```

### Java-API

Dokumentasjon: [http://digipost.github.io/signature-api-client-java](http://digipost.github.io/signature-api-client-java)

Javadoc: [javadoc.io/doc/no.digipost.signature/signature-api-client-java](http://www.javadoc.io/doc/no.digipost.signature/signature-api-client-java)

### Bygge og kjøre tester

Legg inn filen `src/test/java/no/digipost/signering/client/TestKonfigurasjon.java` og fyll med følgende innhold:

```java
package no.digipost.signering.client;

import no.digipost.signering.client.internal.KeyStoreConfig;

public class TestKonfigurasjon {

    public static final KeyStoreConfig CLIENT_KEYSTORE = KeyStoreConfig.fromKeyStore(
            <inputstream med keystore>,
            "<alias>",
            "<keystore passord>",
            "<key inni keystore passord>"
    );

}
```
