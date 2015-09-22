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

public class Dokument implements ASiCEAttachable {

    private String emne;
    private String filnavn;
    private byte[] dokument;
    private String mimeType = "application/pdf";

    public Dokument(final String emne, final String filnavn, final byte[] dokument) {
        this.emne = emne;
        this.filnavn = filnavn;
        this.dokument = dokument;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getFileName() {
        return filnavn;
    }

    @Override
    public byte[] getBytes() {
        return dokument;
    }

    public String getEmne() {
        return emne;
    }

    public String getMimeType() {
        return mimeType;
    }
}
