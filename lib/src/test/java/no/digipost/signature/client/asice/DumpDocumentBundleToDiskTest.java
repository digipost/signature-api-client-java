package no.digipost.signature.client.asice;

import no.digipost.signature.client.core.DocumentType;
import no.digipost.signature.client.direct.DirectDocument;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.direct.DirectSigner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static no.digipost.signature.client.asice.DumpDocumentBundleToDisk.TIMESTAMP_PATTERN;
import static no.digipost.signature.client.direct.ExitUrls.singleExitUrl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class DumpDocumentBundleToDiskTest {

    private static final Clock clock = Clock.fixed(Instant.parse("2026-01-05T12:00:00Z"), ZoneOffset.UTC);
    private static final String timestamp = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN).format(ZonedDateTime.now(clock));

    @TempDir
    Path root;

    @Test
    void reference_with_path_traversal_does_not_escape_dump_directory() throws IOException {
        Path dumpDirectory = Files.createDirectory(root.resolve("dump"));
        // The timestamp prefix "burns" the first path segment, so it must exist as a
        // directory for the traversal to resolve. This mirrors the reported proof of concept.
        Files.createDirectory(dumpDirectory.resolve(timestamp + "-x"));

        DumpDocumentBundleToDisk dumper = new DumpDocumentBundleToDisk(dumpDirectory, clock);
        dumper.process(jobWithReference("x/../../ESCAPED"), new ByteArrayInputStream("bundle".getBytes()));

        assertThat("files written outside the dump directory", filesIn(root), is(empty()));
        assertThat("bundle files written directly in the dump directory", filesIn(dumpDirectory), hasSize(1));
    }

    private static DirectJob jobWithReference(String reference) {
        DirectDocument document = DirectDocument.builder("Document title", "hello".getBytes()).type(DocumentType.TXT).build();
        return DirectJob.builder("Job title",
                    singletonList(document),
                    singletonList(DirectSigner.withPersonalIdentificationNumber("12345678910").build()),
                    singleExitUrl(URI.create("https://job.well.done.org")))
                .withReference(reference)
                .build();
    }

    /**
     * @return regular files located directly in the given directory (not in subdirectories)
     */
    private static List<Path> filesIn(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(Files::isRegularFile).collect(Collectors.toList());
        }
    }

}
