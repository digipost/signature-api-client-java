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
package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.exceptions.ConfigurationException;

import javax.net.ssl.SSLException;
import javax.ws.rs.ProcessingException;

class ClientExceptionMapper {

    public RuntimeException map(ProcessingException e) {
        if (e.getCause() instanceof SSLException) {
            String sslExceptionMessage = e.getCause().getMessage();
            if (sslExceptionMessage != null && sslExceptionMessage.contains("protocol_version")) {
                return new ConfigurationException("Invalid TLS protocol version. This will typically happen if you're running on an older Java version, which doesn't support TLS 1.2. " +
                        "Java 7 needs to be explicitly configured to support TLS 1.2. See 'JSSE tuning parameters' at https://blogs.oracle.com/java-platform-group/entry/diagnosing_tls_ssl_and_https.", e);
            }
        }
        return e;
    }

}
