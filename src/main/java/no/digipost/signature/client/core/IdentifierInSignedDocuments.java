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

import no.digipost.signature.api.xml.XMLIdentifierInSignedDocuments;
import no.digipost.signature.client.core.internal.MarshallableEnum;

public enum IdentifierInSignedDocuments implements MarshallableEnum<XMLIdentifierInSignedDocuments> {

    PERSONAL_IDENTIFICATION_NUMBER_AND_NAME(XMLIdentifierInSignedDocuments.PERSONAL_IDENTIFICATION_NUMBER_AND_NAME),
    DATE_OF_BIRTH_AND_NAME(XMLIdentifierInSignedDocuments.DATE_OF_BIRTH_AND_NAME),
    NAME(XMLIdentifierInSignedDocuments.NAME),
    ;


    private final XMLIdentifierInSignedDocuments xmlEnumValue;

    IdentifierInSignedDocuments(XMLIdentifierInSignedDocuments xmlEnumValue) {
        this.xmlEnumValue = xmlEnumValue;
    }

    @Override
    public XMLIdentifierInSignedDocuments getXmlEnumValue() {
        return xmlEnumValue;
    }
}
