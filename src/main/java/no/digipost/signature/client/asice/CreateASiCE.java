package no.digipost.signature.client.asice;

import no.digipost.signature.client.asice.archive.CreateZip;
import no.digipost.signature.client.asice.manifest.Manifest;
import no.digipost.signature.client.asice.manifest.ManifestCreator;
import no.digipost.signature.client.asice.signature.CreateSignature;
import no.digipost.signature.client.asice.signature.Signature;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureJob;
import no.digipost.signature.client.core.exceptions.RuntimeIOException;
import no.digipost.signature.client.core.internal.ActualSender;
import no.digipost.signature.client.security.KeyStoreConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreateASiCE<JOB extends SignatureJob> {

    private final CreateZip createZip = new CreateZip();
    private final CreateSignature createSignature;

    private final ManifestCreator<JOB> manifestCreator;
    private final Optional<Sender> globalSender;
    private final KeyStoreConfig keyStoreConfig;
    private final Iterable<DocumentBundleProcessor> documentBundleProcessors;

    public CreateASiCE(ManifestCreator<JOB> manifestCreator, ASiCEConfiguration clientConfiguration) {
        this.manifestCreator = manifestCreator;
        this.globalSender = clientConfiguration.getGlobalSender();
        this.keyStoreConfig = clientConfiguration.getKeyStoreConfig();
        this.documentBundleProcessors = clientConfiguration.getDocumentBundleProcessors();
        this.createSignature = new CreateSignature(clientConfiguration.getClock());
    }

    public DocumentBundle createASiCE(JOB job) {
        Sender sender = ActualSender.getActualSender(job.getSender(), globalSender);
        Manifest manifest = manifestCreator.createManifest(job, sender);

        List<ASiCEAttachable> files = new ArrayList<>(job.getDocuments());
        files.add(manifest);

        Signature signature = createSignature.createSignature(files, keyStoreConfig);
        files.add(signature);

        byte[] zipped = createZip.zipIt(files);
        for (DocumentBundleProcessor processor : documentBundleProcessors) {
            try (ByteArrayInputStream zipStream = new ByteArrayInputStream(zipped)) {
                processor.process(job, zipStream);
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        }

        return new DocumentBundle(zipped);
    }

}
