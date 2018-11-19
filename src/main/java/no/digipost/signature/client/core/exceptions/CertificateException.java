package no.digipost.signature.client.core.exceptions;

public class CertificateException extends ConfigurationException {

    public CertificateException(String message, Exception e) {
        super(message, e);
    }

    public CertificateException(String message) {
        super(message);
    }

}
