# Hvordan få ting til å bygge og kjøre tester

Legg inn filen `src/test/java/no.digipost.signering.client/TestKonfigurasjon.java` og fyll med følgende innhold:

```java
package no.digipost.signering.client;

import no.digipost.signering.client.internal.CertStoreConfig;

public class TestKonfigurasjon {

	public static final CertStoreConfig CLIENT_KEYSTORE = new CertStoreConfig(
			"<keystore path>",
			"<keystore passord>",
			"<key inni keystore passord>"
	);

}
```