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
import no.digipost.signering.schema.v1.signature_job.XMLDirectSignatureJobStatus;

public class SignatureJobStatusResponse {

    private long signatureJobId;
    private String reference;
    private SignatureJobStatus status;
    private XAdESReference xAdESUrl;
    private PAdESReference pAdESUrl;
    private ConfirmationReference confirmationUrl;

    public SignatureJobStatusResponse(long signatureJobId, String reference, XMLDirectSignatureJobStatus status, String xAdESUrl, String pAdESUrl, String confirmationUrl) {
        this.signatureJobId = signatureJobId;
        this.status = SignatureJobStatus.fromXmlType(status);
        this.reference = reference;
        this.xAdESUrl = new XAdESReference(xAdESUrl);
        this.pAdESUrl = new PAdESReference(pAdESUrl);
        this.confirmationUrl = new ConfirmationReference(confirmationUrl);
    }

    public SignatureJobStatusResponse(long signatureJobId, String reference, XMLDirectSignatureJobStatus status) {
        this(signatureJobId, reference, status, null, null, null);
    }

    public long getSignatureJobId() {
        return signatureJobId;
    }

    public SignatureJobStatus getStatus() {
        return status;
    }

    public String getReference() {
        return reference;
    }

    public XAdESReference getxAdESUrl() {
        return xAdESUrl;
    }

    public PAdESReference getpAdESUrl() {
        return pAdESUrl;
    }

    public ConfirmationReference getConfirmationUrl() {
        return confirmationUrl;
    }
}
