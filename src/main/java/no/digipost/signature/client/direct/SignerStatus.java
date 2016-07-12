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

import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

public class SignerStatus {

    /**
     * The signer has rejected to sign the document.
     */
    public static final SignerStatus REJECTED = new SignerStatus("REJECTED");

    /**
     * The signer has not made a decision to either sign or reject the document within the
     * specified time limit.
     */
    public static final SignerStatus EXPIRED = new SignerStatus("EXPIRED");

    /**
     * The signer has yet to review the document and decide if she/he wants to sign or
     * reject it.
     */
    public static final SignerStatus WAITING = new SignerStatus("WAITING");

    /**
     * The signer has successfully signed the document.
     */
    public static final SignerStatus SIGNED = new SignerStatus("SIGNED");
    /**
     * The signer has successfully signed the document.
     */
    public static final SignerStatus FAILED = new SignerStatus("FAILED");


    private static final List<SignerStatus> KNOWN_STATUSES = asList(
            REJECTED,
            EXPIRED,
            WAITING,
            SIGNED,
            FAILED
    );

    private final String identifier;


    public SignerStatus(String identifier) {
        this.identifier = identifier;
    }

    public static SignerStatus fromXmlType(String xmlSignerStatus) {
        for (SignerStatus status : KNOWN_STATUSES) {
            if (status.is(xmlSignerStatus)) {
                return status;
            }
        }

        return new SignerStatus(xmlSignerStatus);
    }

    private boolean is(String xmlSignerStatus) {
        return this.identifier.equals(xmlSignerStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SignerStatus) {
            SignerStatus that = (SignerStatus) o;
            return Objects.equals(identifier, that.identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return identifier;
    }

}
