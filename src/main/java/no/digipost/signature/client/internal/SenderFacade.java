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
package no.digipost.signature.client.internal;

import no.digipost.signature.client.ClientConfiguration;
import no.digipost.signature.client.asice.DocumentBundle;
import no.digipost.signature.client.domain.exceptions.SendException;
import no.digipost.signering.schema.v1.SignableDocument;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.IOException;

import static org.apache.commons.io.Charsets.UTF_8;

public class SenderFacade {

    public static final String SIGNATURE_REQUESTS_PATH = "/signatureRequest";

    private final ClientConfiguration clientConfiguration;
    private final CloseableHttpClient httpClient;
    private final Jaxb2Marshaller marshaller;

    public SenderFacade(final ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
        httpClient = SigneringHttpClient.create(clientConfiguration.getKeyStoreConfig());
        marshaller = Marshalling.instance();
    }

    public SignableDocument createSignatureRequest(final DocumentBundle documentBundle) {
        HttpPost request = new HttpPost(clientConfiguration.getSignatureServiceRoot() + SIGNATURE_REQUESTS_PATH);
        request.setEntity(new ByteArrayEntity(documentBundle.getBytes(), ContentType.APPLICATION_OCTET_STREAM));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return Marshalling.unmarshal(marshaller, response.getEntity().getContent(), SignableDocument.class);
            } else {
                throw new SendException(EntityUtils.toString(response.getEntity(), UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to server.", e);
        }
    }

    public String tryConnecting() {
        try {
            CloseableHttpResponse response = httpClient.execute(new HttpGet(clientConfiguration.getSignatureServiceRoot()));
            return EntityUtils.toString(response.getEntity(), UTF_8);
        } catch (IOException e) {
            // TODO (EHH): Innføre en egen exception-type?
            throw new RuntimeException("Unable to connect to server.", e);
        }
    }
}
