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
package no.digipost.signature.client.core.exceptions;

import no.motif.f.Fn0;

public class SignerNotSpecifiedException extends SignatureException {

    public static final Fn0<SignatureException> SIGNER_NOT_SPECIFIED = new Fn0<SignatureException>() {
        @Override
        public SignatureException $() {
            return new SignerNotSpecifiedException();
        }
    };

    private SignerNotSpecifiedException() {
        super("Signer's personal identification number must be specified.");
    }
}
