/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.signature.client.asice;

import no.digipost.signature.client.core.SignatureJob;
import no.motif.f.Fn;
import no.motif.single.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.nio.file.Files.isDirectory;
import static no.motif.Base.first;
import static no.motif.Singular.optional;
import static no.motif.Strings.append;
import static no.motif.Strings.inBetween;

public class DumpDocumentBundleToDisk implements DocumentBundleProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DumpDocumentBundleToDisk.class);

    private static final String TIMESTAMP_PATTERN = "yyyyMMddHHmmssSSS";

    private final Path directory;

    public DumpDocumentBundleToDisk(Path directory) {
        this.directory = directory;
    }


    @Override
    public void process(SignatureJob job, InputStream documentBundle) throws IOException {
        if (isDirectory(directory)) {
            DateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_PATTERN);
            Optional<String> reference = optional(job.getReference());
            String filename = timestampFormat.format(new Date()) + "-" + reference.map(referenceFilenamePart).orElse("") + "asice.zip";
            Path target = directory.resolve(filename);
            LOG.info("Dumping document bundle{}to {}", reference.map(inBetween(" for job with reference '", "' ")).orElse(" "), target);
            Files.copy(documentBundle, target);
        } else {
            throw new InvalidDirectoryException(directory);
        }
    }

    public static class InvalidDirectoryException extends IOException {
        public InvalidDirectoryException(Path path) {
            super("The path " + path + (!Files.exists(path) ? " does not exist" : " is not a valid directory"));
        }
    }

    static final Fn<String, String> referenceFilenamePart = first(new Fn<String, String>() {
        @Override
        public String $(String reference) {
            return reference.replace(' ', '_');
        }
    }).then(append("-"));

}
