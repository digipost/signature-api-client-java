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
