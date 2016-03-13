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
package no.digipost.signature.client.core.internal.http;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import java.net.URI;

public class SignatureHttpClientFactory {


    public static SignatureHttpClient create(HttpIntegrationConfiguration config) {
        Client jerseyClient = JerseyClientBuilder.newBuilder()
                .withConfig(config.getJaxrsConfiguration())
                .sslContext(config.getSSLContext())
                .hostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        return new DefaultClient(jerseyClient, config.getServiceRoot());
    }



    private static final class DefaultClient implements SignatureHttpClient {

        private final Client jerseyClient;
        private final WebTarget signatureServiceRoot;

        DefaultClient(Client jerseyClient, URI root) {
            this.jerseyClient = jerseyClient;
            this.signatureServiceRoot = jerseyClient.target(root);
        }

        @Override
        public WebTarget target(String uri) {
            return jerseyClient.target(uri);
        }

        @Override
        public WebTarget signatureServiceRoot() {
            return signatureServiceRoot;
        }

    }

}
