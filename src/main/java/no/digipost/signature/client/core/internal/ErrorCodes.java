package no.digipost.signature.client.core.internal;

public enum ErrorCodes {

    BROKER_NOT_AUTHORIZED,
    SIGNING_CEREMONY_NOT_COMPLETED,
    INVALID_STATUS_QUERY_TOKEN;

    public boolean sameAs(String other) {
        return this.name().equals(other);
    }
}
