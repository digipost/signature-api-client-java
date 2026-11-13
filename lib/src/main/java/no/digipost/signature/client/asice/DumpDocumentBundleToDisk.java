package no.digipost.signature.client.asice;

import no.digipost.signature.client.core.SignatureJob;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static java.nio.file.Files.isDirectory;

public class DumpDocumentBundleToDisk implements DocumentBundleProcessor {

    private static final Logger LOG = Logger.getLogger(DumpDocumentBundleToDisk.class.getName());

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
            Path dumpDirectory = directory.toAbsolutePath().normalize();
            Path target = dumpDirectory.resolve(filename).normalize();
            if (!dumpDirectory.equals(target.getParent())) {
                throw new IOException("Refusing to dump document bundle to " + target + ", which is not directly inside " + dumpDirectory);
            }
            LOG.info(() -> "Dumping document bundle" + reference.map(ref -> " for job with reference '" + ref + "'").orElse("") + " to " + target);
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

    /**
     * Characters not in this set are replaced, so a job reference can never introduce
     * path separators or other characters with special meaning in a file name.
     */
    private static final Pattern UNSAFE_FILENAME_CHARS = Pattern.compile("[^A-Za-z0-9._-]");

    /**
     * Sequences of two or more dots are replaced to avoid "{@code ..}" in file names.
     */
    private static final Pattern DOT_SEQUENCES = Pattern.compile("\\.{2,}");

    static final Function<String, String> referenceFilenamePart = reference -> {
        String safeChars = UNSAFE_FILENAME_CHARS.matcher(reference).replaceAll("_");
        return DOT_SEQUENCES.matcher(safeChars).replaceAll("_") + "-";
    };

}
