package no.digipost.signature.client.asice;

import no.digipost.signature.client.core.internal.MaySpecifySender;
import no.digipost.signature.client.security.KeyStoreConfig;

import java.time.Clock;

public interface ASiCEConfiguration {

    KeyStoreConfig getKeyStoreConfig();

    MaySpecifySender getDefaultSender();

    Iterable<DocumentBundleProcessor> getDocumentBundleProcessors();

    Clock getClock();

}
