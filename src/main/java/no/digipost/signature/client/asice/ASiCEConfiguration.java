package no.digipost.signature.client.asice;

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.security.KeyStoreConfig;

import java.time.Clock;
import java.util.Optional;

public interface ASiCEConfiguration {

    KeyStoreConfig getKeyStoreConfig();

    Optional<Sender> getGlobalSender();

    Iterable<DocumentBundleProcessor> getDocumentBundleProcessors();

    Clock getClock();

}
