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

import no.digipost.signature.client.core.SignatureType;

/**
 * Provides operations for customizing signers using builder-type methods for
 * properties which are common for both Direct and Portal signers.
 * You would not under normal circumstances refer to this type.
 */
public interface SignerCustomizations<B extends SignerCustomizations<B>> {

    /**
     * Specify the {@link SignatureType type of signature} to use for the signer.
     *
     * @param type the {@link SignatureType}
     */
    B withSignatureType(SignatureType type);

}
