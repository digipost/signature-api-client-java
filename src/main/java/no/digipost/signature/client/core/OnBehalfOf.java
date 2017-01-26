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

import no.digipost.signature.api.xml.XMLSigningOnBehalfOf;
import no.digipost.signature.client.core.internal.MarshallableEnum;

/**
 * Specifies if the signer signs on behalf of itself or some other party
 */
public enum OnBehalfOf implements MarshallableEnum<XMLSigningOnBehalfOf> {

    SELF(XMLSigningOnBehalfOf.SELF),
    OTHER(XMLSigningOnBehalfOf.OTHER);

    private final XMLSigningOnBehalfOf xmlEnumValue;

    OnBehalfOf(XMLSigningOnBehalfOf xmlEnumValue) {
        this.xmlEnumValue = xmlEnumValue;
    }

    @Override
    public XMLSigningOnBehalfOf getXmlEnumValue() {
        return xmlEnumValue;
    }

}
