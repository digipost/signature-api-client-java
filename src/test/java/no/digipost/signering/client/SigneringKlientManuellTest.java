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

import no.digipost.signering.client.domain.Tjenesteeier;
import org.junit.Test;

import java.net.URI;

public class SigneringKlientManuellTest {

	@Test
	public void skal_koble_til_via_toveis_ssl() {
		SigneringKlient signeringKlient = new SigneringKlient(new Tjenesteeier("984661185"), new KlientKonfigurasjon(URI.create("https://localhost:8443"), TestKonfigurasjon.CLIENT_KEYSTORE));
		String responsString = signeringKlient.tryConnecting();
		System.out.println("Fikk følgende fra serveren:\n" + responsString);
	}
}
