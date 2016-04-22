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
package no.digipost.signature.client.direct;

import no.digipost.signature.client.core.Document;

import static no.digipost.signature.client.core.Document.FileType.PDF;

public class DirectDocument extends Document {
    private DirectDocument(String subject, String message, String fileName, FileType fileType, byte[] document) {
        super(subject, message, fileName, fileType, document);
    }

    public static Builder builder(final String subject, final String fileName, final byte[] document) {
        return new Builder(subject, fileName, document);
    }

    public static class Builder {

        private String subject;
        private String fileName;
        private byte[] document;
        private String message;
        private FileType fileType = PDF;

        public Builder(final String subject, final String fileName, final byte[] document) {
            this.subject = subject;
            this.fileName = fileName;
            this.document = document;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder fileType(final FileType fileType) {
            this.fileType = fileType;
            return this;
        }

        public DirectDocument build() {
            return new DirectDocument(subject, message, fileName, fileType, document);
        }
    }
}
