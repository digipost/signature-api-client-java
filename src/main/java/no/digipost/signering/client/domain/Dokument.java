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
package no.digipost.signering.client.domain;

import no.digipost.signering.client.asice.ASiCEAttachable;

import static no.digipost.signering.client.domain.Dokument.FileType.PDF;

public class Dokument implements ASiCEAttachable {

    private String emne;
    private String filnavn;
    private byte[] dokument;
    private FileType fileType = PDF;

    private Dokument(final String emne, final String filnavn, final byte[] dokument) {
        this.emne = emne;
        this.filnavn = filnavn;
        this.dokument = dokument;
    }

    @Override
    public String getFileName() {
        return filnavn;
    }

    @Override
    public byte[] getBytes() {
        return dokument;
    }

    @Override
    public String getMimeType() {
        return fileType.mimeType;
    }

    public String getEmne() {
        return emne;
    }

    public static Builder builder(final String emne, final String filnavn, final byte[] dokument) {
        return new Builder(emne, filnavn, dokument);
    }

    public static class Builder {

        private final Dokument target;
        private boolean built = false;

        public Builder(final String emne, final String filnavn, final byte[] dokument) {
            this.target = new Dokument(emne, filnavn, dokument);
        }

        public Builder fileType(final FileType fileType) {
            target.fileType = fileType;
            return this;
        }

        public Dokument build() {
            if (built) throw new IllegalStateException("Can't build twice");
            built = true;
            return target;
        }
    }

    public enum FileType {
        PDF("application/pdf"),
        TXT("text/plain");

        public final String mimeType;

        FileType(final String mimeType) {
            this.mimeType = mimeType;
        }
    }
}
