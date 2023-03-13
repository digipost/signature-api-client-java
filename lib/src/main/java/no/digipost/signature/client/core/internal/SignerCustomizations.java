package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.OnBehalfOf;
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

    /**
     * Specify which party the signer is {@link OnBehalfOf signing on behalf of}.
     *
     * @param onBehalfOf the {@link OnBehalfOf}-value
     */
    B onBehalfOf(OnBehalfOf onBehalfOf);

}
