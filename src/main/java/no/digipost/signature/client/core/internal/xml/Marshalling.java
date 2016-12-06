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
package no.digipost.signature.client.core.internal.xml;

import no.digipost.signature.jaxb.spring.SignatureJaxb2Marshaller;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.InputStream;
import java.io.OutputStream;

public final class Marshalling {

    public static void marshal(Object object, OutputStream entityStream) {
        SignatureJaxb2Marshaller.ForRequestsOfAllApis.singleton().marshal(object, new StreamResult(entityStream));
    }

    public static Object unmarshal(InputStream entityStream) {
        return SignatureJaxb2Marshaller.ForResponsesOfAllApis.singleton().unmarshal(new StreamSource(entityStream));
    }

    private Marshalling() { }
}
