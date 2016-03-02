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

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StatusReferenceTest {

    @Test
    public void buildsCorrectUrlWithToken() {
        String statusUrl = "https://statusqueryservice/status/?job=1337";
        String token = "abcdefgh";
        StatusReference statusReference = new StatusReference(statusUrl, token);
        assertThat(statusReference.getStatusUrl(), is(statusUrl + "&" + StatusReference.STATUS_QUERY_TOKEN_PARAM_NAME + "=" + token));
    }
}
