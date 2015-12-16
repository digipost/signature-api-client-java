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

import no.digipost.signature.client.core.ConfirmationReference;
import no.digipost.signature.client.core.PAdESReference;
import no.digipost.signature.client.core.XAdESReference;
import no.digipost.signature.client.core.internal.Confirmable;


public class SignatureJobStatusResponse implements Confirmable {

    private long signatureJobId;
    private SignatureJobStatus status;
    private XAdESReference xAdESUrl;
    private PAdESReference pAdESUrl;
    private ConfirmationReference confirmationReference;

    public SignatureJobStatusResponse(long signatureJobId, SignatureJobStatus status, String xAdESUrl, String pAdESUrl, String confirmationUrl) {
        this.signatureJobId = signatureJobId;
        this.status = status;
        this.xAdESUrl = new XAdESReference(xAdESUrl);
        this.pAdESUrl = new PAdESReference(pAdESUrl);
        this.confirmationReference = new ConfirmationReference(confirmationUrl);
    }

    public SignatureJobStatusResponse(long signatureJobId, SignatureJobStatus status) {
        this(signatureJobId, status, null, null, null);
    }

    public long getSignatureJobId() {
        return signatureJobId;
    }

    public SignatureJobStatus getStatus() {
        return status;
    }

    public XAdESReference getxAdESUrl() {
        return xAdESUrl;
    }

    public PAdESReference getpAdESUrl() {
        return pAdESUrl;
    }

    @Override
    public ConfirmationReference getConfirmationReference() {
        return confirmationReference;
    }
}
