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
package no.digipost.signering.client;

import no.digipost.signering.client.asice.CreateASiCE;
import no.digipost.signering.client.asice.DocumentBundle;
import no.digipost.signering.client.domain.Signeringsoppdrag;
import no.digipost.signering.client.domain.Tjenesteeier;
import no.digipost.signering.client.internal.SigneringHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class SigneringKlient {

    private static final Logger LOG = LoggerFactory.getLogger(SigneringKlient.class);

    private Tjenesteeier tjenesteeier;
    private KlientKonfigurasjon klientKonfigurasjon;
    private CreateASiCE dokumentpakkeBuilder;
    private final CloseableHttpClient httpClient;

    public SigneringKlient(Tjenesteeier tjenesteeier, KlientKonfigurasjon klientKonfigurasjon) {
        this.tjenesteeier = tjenesteeier;
        this.klientKonfigurasjon = klientKonfigurasjon;
        httpClient = SigneringHttpClient.create(klientKonfigurasjon.getKeystoreConfig());
        this.dokumentpakkeBuilder = new CreateASiCE();
    }

    public void opprett(final Signeringsoppdrag signeringsoppdrag) {
        DocumentBundle documentBundle = dokumentpakkeBuilder.createASiCE(signeringsoppdrag);

        // TODO (SBN) Nice-ify code for communicating over HTTP
        HttpPost post = new HttpPost(klientKonfigurasjon.getSigneringstjenesteRoot() + "/oppdrag");
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream(documentBundle.getBytes()));
        post.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            System.out.println(convertStreamToString(response.getEntity().getContent()));
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke koble til server.", e);
        }
    }

    public String tryConnecting() {
        try {
            CloseableHttpResponse response = httpClient.execute(new HttpGet(klientKonfigurasjon.getSigneringstjenesteRoot()));
            String responseString = convertStreamToString(response.getEntity().getContent());
            LOG.debug("Server svarte med følgende respons:\n" + responseString);
            if (!responseString.contains(tjenesteeier.getOrganisasjonsNummer())) {
                // TODO (EHH): Innføre en egen exception-type?
                throw new RuntimeException("Fikk ikke organisasjonsnummer tilbake fra server. Noe er galt i oppsettet.");
            }
            return responseString;
        } catch (IOException e) {
            // TODO (EHH): Innføre en egen exception-type?
            throw new RuntimeException("Kunne ikke koble til server.", e);
        }
    }

    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
