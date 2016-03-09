# Posten Signering – Java Klientbibliotek

Dette repoet inneholder klientbibliotek implementert i Java som kan benyttes for å integrere mot Posten Signering.

Nærmere dokumentasjon av APIet og detaljer rundt bruk kan finnes i repoet [Signature-API-Specification](https://github.com/digipost/signature-api-specification)

## Endringshyppighet på klientbibliotek og API
Dette klientbiblioteket og APIene er pr. i dag "work in progress", og det vil forekomme endringer utover våren 2016. Sørg for å avklare med din kontakt i Posten/Difi før du baserer deg på noen av disse bibliotekene og APIene. På grunn av dette, så finnes det pr. i dag heller ingen releasede versjoner av klientene.


---


## Bruke biblioteket


### Maven-avhengighet

Klientbiblioteket er releaset til [![Maven Central](https://maven-badges.herokuapp.com/maven-central/no.digipost.signature/signature-api-client-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/no.digipost.signature/signature-api-client-java). For å avhenge av biblioteket, legg til følgende i `pom.xml`:

```xml
<dependency>
    <groupId>no.digipost.signature</groupId>
    <artifactId>signature-api-client-java</artifactId>
    <version></version>
</dependency>
```

### Bygge og kjøre tester

Legg inn filen `src/test/java/no/digipost/signering/client/TestKonfigurasjon.java` og fyll med følgende innhold:

```java
package no.digipost.signering.client;

import no.digipost.signering.client.internal.KeyStoreConfig;

public class TestKonfigurasjon {

    public static final KeyStoreConfig CLIENT_KEYSTORE = KeyStoreConfig.fraKeyStore(
            <inputstream med keystore>,
            "<alias>",
            "<keystore passord>",
            "<key inni keystore passord>"
    );

}
```


### Java-API

*Mer informasjon vil komme senere, inntil da kan du se på dokumentasjonen i klassene `DirectClient` og `PortalClient`.*

Javadoc: [javadoc.io/doc/no.digipost.signature/signature-api-client-java](http://www.javadoc.io/doc/no.digipost.signature/signature-api-client-java)

---


## Feilsøking

Her er noen tips for hvordan man kan gå frem for å undersøke oppførselen til bibliteket i eventuelle feilsøkingssituasjoner.

### Logge request og respons

Klientbiblioteket kan konfigureres til å logge HTTP-requester og -responser ved å kalle `enableRequestAndResponseLogging()` når man bygger opp klientens konfigurasjon. Man kan da konfigurere loggeren  `no.digipost.signature.client.http.requestresponse` for å tilpasse loggingen. Den må settes til minst `INFO`-nivå for å skrive til loggen.


Logging av requests og respons skjer per dags dato med en `java.util.logging`-logger via Jersey 2 sitt [LoggingFilter](https://jersey.java.net/apidocs/latest/jersey/org/glassfish/jersey/filter/LoggingFilter.html). Klientbiblioteket benytter ellers SLF4J til logging, og dersom man ønsker å konfigurere request- og respons-logging på samme måte som man konfigurerer logging ellers via SLF4J, må man installere [jul-to-slf4j bridge](http://www.slf4j.org/legacy.html#jul-to-slf4j). Som nevnt på siden er det en ytelsesutfordring forbundet med denne mekanismen, og dersom man bruker Logback som logger-implementasjon, anbefales det å i tillegg konfigurere [LevelChangePropagator](http://logback.qos.ch/manual/configuration.html#LevelChangePropagator).
