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
package no.digipost.signature.client.direct;

import javax.ws.rs.core.UriBuilder;

import java.util.Objects;

public class StatusReference {

    public static final String STATUS_QUERY_TOKEN_PARAM_NAME = "status_query_token";

    private final String statusUrl;
    private final String statusQueryToken;

    public StatusReference(String statusUrl, String statusQueryToken) {
        this.statusUrl = statusUrl;
        this.statusQueryToken = Objects.requireNonNull(statusQueryToken, STATUS_QUERY_TOKEN_PARAM_NAME);
    }

    public String getStatusUrl() {
        return UriBuilder.fromUri(statusUrl).queryParam(STATUS_QUERY_TOKEN_PARAM_NAME, statusQueryToken).build().toString();
    }
}
