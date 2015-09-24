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
import no.digipost.signering.client.internal.SenderFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SigneringKlient {

    private static final Logger LOG = LoggerFactory.getLogger(SigneringKlient.class);

    private final Tjenesteeier tjenesteeier;
    private final KlientKonfigurasjon klientKonfigurasjon;
    private final CreateASiCE dokumentpakkeBuilder;
    private final SenderFacade senderFacade;

    public SigneringKlient(Tjenesteeier tjenesteeier, KlientKonfigurasjon klientKonfigurasjon) {
        this.tjenesteeier = tjenesteeier;
        this.klientKonfigurasjon = klientKonfigurasjon;
        this.dokumentpakkeBuilder = new CreateASiCE();
        this.senderFacade = new SenderFacade(klientKonfigurasjon);
    }

    public String opprett(final Signeringsoppdrag signeringsoppdrag) {
        DocumentBundle documentBundle = dokumentpakkeBuilder.createASiCE(signeringsoppdrag, klientKonfigurasjon.getKeyStoreConfig());

        return senderFacade.opprettSigneringsoppdrag(documentBundle);
    }

    public String tryConnecting() {
        String responseString = senderFacade.tryConnecting();
        LOG.debug("Server svarte med følgende respons:\n" + responseString);
        if (!responseString.contains(tjenesteeier.getOrganisasjonsNummer())) {
            // TODO (EHH): Innføre en egen exception-type?
            throw new RuntimeException("Fikk ikke organisasjonsnummer tilbake fra server. Noe er galt i oppsettet.");
        }
        return responseString;
    }

}
