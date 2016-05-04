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
package no.digipost.signature.client.portal;

import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

public class SignatureStatus {

    public static final SignatureStatus REJECTED = new SignatureStatus("REJECTED");
    public static final SignatureStatus CANCELLED = new SignatureStatus("CANCELLED");
    public static final SignatureStatus RESERVED = new SignatureStatus("RESERVED");
    public static final SignatureStatus CONTACT_INFORMATION_MISSING = new SignatureStatus("CONTACT_INFORMATION_MISSING");
    public static final SignatureStatus EXPIRED = new SignatureStatus("EXPIRED");
    public static final SignatureStatus WAITING = new SignatureStatus("WAITING");
    public static final SignatureStatus SIGNED = new SignatureStatus("SIGNED");

    private static final List<SignatureStatus> KNOWN_STATUSES = asList(
            REJECTED,
            CANCELLED,
            RESERVED,
            CONTACT_INFORMATION_MISSING,
            EXPIRED,
            WAITING,
            SIGNED
    );

    private final String identifier;

    public SignatureStatus(String identifier) {
        this.identifier = identifier;
    }

    public static SignatureStatus fromXmlType(String xmlSignatureStatus) {
        for (SignatureStatus status : KNOWN_STATUSES) {
            if (status.is(xmlSignatureStatus)) {
                return status;
            }
        }

        return new SignatureStatus(xmlSignatureStatus);
    }

    private boolean is(String xmlSignatureStatus) {
        return this.identifier.equals(xmlSignatureStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureStatus that = (SignatureStatus) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
