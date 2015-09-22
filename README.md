# Hvordan få ting til å bygge og kjøre tester

Legg inn filen `src/test/java/no.digipost.signering.client/TestKonfigurasjon.java` og fyll med følgende innhold:

```java
package no.digipost.signering.client;

import no.digipost.signering.client.internal.KeyStoreConfig;

public class TestKonfigurasjon {

    public static final KeyStoreConfig CLIENT_KEYSTORE = KeyStoreConfig.fraKeyStore(
            "<keystore path>",
            "<alias>",
            "<keystore passord>",
            "<key inni keystore passord>"
    );

}
```
