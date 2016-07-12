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

import no.motif.Singular;
import no.motif.single.Optional;

public class XAdESReference {

    public static XAdESReference of(String url) {
        return of(Singular.optional(url));
    }

    public static XAdESReference of(Optional<String> url) {
        return url.isSome() ? new XAdESReference(url.get()) : null;
    }

    private final String xAdESUrl;

    private XAdESReference(String xAdESUrl) {
        this.xAdESUrl = xAdESUrl;
    }

    public String getxAdESUrl() {
        return xAdESUrl;
    }
}
