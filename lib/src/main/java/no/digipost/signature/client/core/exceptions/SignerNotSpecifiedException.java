package no.digipost.signature.client.core.exceptions;

import java.util.function.Supplier;

public class SignerNotSpecifiedException extends SignatureException {

    public static final Supplier<SignatureException> SIGNER_NOT_SPECIFIED = SignerNotSpecifiedException::new;

    private SignerNotSpecifiedException() {
        super("Signer's personal identification number must be specified.");
    }
}
