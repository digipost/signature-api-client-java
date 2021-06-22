package no.digipost.signature.client.asice.archive;

import no.digipost.signature.client.asice.ASiCEAttachable;
import no.digipost.signature.client.core.exceptions.RuntimeIOException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CreateZip {

    public byte[] zipIt(final List<ASiCEAttachable> files) {
        try (ByteArrayOutputStream archive = new ByteArrayOutputStream()) {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(archive)) {
                for (ASiCEAttachable file : files) {
                    ZipEntry zipEntry = new ZipEntry(file.getFileName());
                    zipEntry.setSize(file.getContent().length);
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.write(file.getContent());
                    zipOutputStream.closeEntry();
                }
            }
            return archive.toByteArray();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }

    }
}
