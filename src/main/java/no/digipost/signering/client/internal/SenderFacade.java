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
package no.digipost.signering.client.internal;

import no.digipost.signering.client.KlientKonfigurasjon;
import no.digipost.signering.client.asice.DocumentBundle;
import no.digipost.signering.client.domain.exceptions.SendException;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static org.apache.commons.io.Charsets.UTF_8;

public class SenderFacade {

    public static final String SIGNERINGSOPPDRAG_PATH = "/oppdrag";

    private final KlientKonfigurasjon klientKonfigurasjon;
    private final CloseableHttpClient httpClient;

    public SenderFacade(final KlientKonfigurasjon klientKonfigurasjon) {
        this.klientKonfigurasjon = klientKonfigurasjon;
        httpClient = SigneringHttpClient.create(klientKonfigurasjon.getKeyStoreConfig());
    }

    public String opprettSigneringsoppdrag(final DocumentBundle documentBundle) {
        HttpPost request = new HttpPost(klientKonfigurasjon.getSigneringstjenesteRoot() + SIGNERINGSOPPDRAG_PATH);
        request.setEntity(new ByteArrayEntity(documentBundle.getBytes(), ContentType.APPLICATION_OCTET_STREAM));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                return response.getFirstHeader("Location").getValue();
            } else {
                throw new SendException(EntityUtils.toString(response.getEntity(), UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke koble til server.", e);
        }
    }

    public String tryConnecting() {
        try {
            CloseableHttpResponse response = httpClient.execute(new HttpGet(klientKonfigurasjon.getSigneringstjenesteRoot()));
            return EntityUtils.toString(response.getEntity(), UTF_8);
        } catch (IOException e) {
            // TODO (EHH): Innføre en egen exception-type?
            throw new RuntimeException("Kunne ikke koble til server.", e);
        }
    }
}
