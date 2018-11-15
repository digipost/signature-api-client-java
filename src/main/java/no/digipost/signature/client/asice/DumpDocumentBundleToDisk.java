package no.digipost.signature.client.asice;

import no.digipost.signature.client.core.SignatureJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static java.nio.file.Files.isDirectory;

public class DumpDocumentBundleToDisk implements DocumentBundleProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DumpDocumentBundleToDisk.class);

    static final String TIMESTAMP_PATTERN = "yyyyMMddHHmmssSSS";

    private final Path directory;
    private final Clock clock;

    public DumpDocumentBundleToDisk(Path directory, Clock clock) {
        this.directory = directory;
        this.clock = clock;
    }


    @Override
    public void process(SignatureJob job, InputStream documentBundle) throws IOException {
        if (isDirectory(directory)) {
            DateTimeFormatter timestampFormat = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);
            Optional<String> reference = Optional.ofNullable(job.getReference());
            String filename = timestampFormat.format(ZonedDateTime.now(clock)) + "-" + reference.map(referenceFilenamePart).orElse("") + "asice.zip";
            Path target = directory.resolve(filename);
            LOG.info("Dumping document bundle{}to {}", reference.map(ref -> format(" for job with reference '%s' ", ref)).orElse(" "), target);
            Files.copy(documentBundle, target);
        } else {
            throw new InvalidDirectoryException(directory);
        }
    }

    public static class InvalidDirectoryException extends IOException {
        InvalidDirectoryException(Path path) {
            super("The path " + path + (!Files.exists(path) ? " does not exist" : " is not a valid directory"));
        }
    }

    static final Function<String, String> referenceFilenamePart = reference -> reference.replace(' ', '_') + "-";

}
