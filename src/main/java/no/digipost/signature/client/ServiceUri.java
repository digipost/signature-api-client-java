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

import java.net.URI;

public enum ServiceUri {
    PRODUCTION(URI.create("https://api.signering.posten.no/api")),
    DIFI_QA(URI.create("https://api.difiqa.signering.posten.no/api")),
    DIFI_TEST(URI.create("https://api.difitest.signering.posten.no/api"));

    final URI uri;

    ServiceUri(URI uri) {
        this.uri = uri;
    }
}