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
package no.digipost.signature.client.core;

import no.digipost.signature.client.asice.ASiCEAttachable;

public abstract class Document implements ASiCEAttachable {

    private String subject;
    private String message;
    private String fileName;
    private byte[] document;
    private FileType fileType;

    protected Document(final String subject, final String message, final String fileName, final FileType fileType, final byte[] document) {
        this.subject = subject;
        this.message = message;
        this.fileName = fileName;
        this.fileType = fileType;
        this.document = document;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public byte[] getBytes() {
        return document;
    }

    @Override
    public String getMimeType() {
        return fileType.mimeType;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
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
