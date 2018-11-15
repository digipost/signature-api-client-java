package no.digipost.signature.client.asice;

import no.digipost.signature.client.core.SignatureJob;

import java.io.IOException;
import java.io.InputStream;

public interface DocumentBundleProcessor {

    void process(SignatureJob job, InputStream documentBundleStream) throws IOException;

}
