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
package no.digipost.signature.client;

import org.junit.Before;
import org.junit.Test;

import static no.digipost.signature.client.ClientConfiguration.MANDATORY_USER_AGENT;
import static no.digipost.signature.client.ClientMetadata.VERSION;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ClientConfigurationTest {

    private ClientConfiguration.Builder config;

    @Before
    public void instantiateConfigBuilder() {
        config = ClientConfiguration.builder(TestKonfigurasjon.CLIENT_KEYSTORE);
    }

    @Test
    public void givesDefaultUserAgent() {
        assertThat(config.build().generateUserAgentString(), both(is(MANDATORY_USER_AGENT)).and(containsString(VERSION)));
    }

    @Test
    public void appendsCustomUserAgentAfterDefault() {
        assertThat(config.includeInUserAgent("My Corporation").build().generateUserAgentString(),
                both(startsWith(MANDATORY_USER_AGENT))
                .and(containsString(VERSION))
                .and(containsString("My Corporation")));
    }
}
