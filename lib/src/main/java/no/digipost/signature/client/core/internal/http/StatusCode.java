package no.digipost.signature.client.core.internal.http;

import java.util.EnumSet;
import java.util.Set;

public final class StatusCode {

    public enum Family {
        INFORMATIONAL, SUCCESSFUL, REDIRECTION, CLIENT_ERROR, SERVER_ERROR, OTHER;

        public static Family of(int statusCode) {
            switch (statusCode / 100) {
                case 1: return INFORMATIONAL;
                case 2: return SUCCESSFUL;
                case 3: return REDIRECTION;
                case 4: return CLIENT_ERROR;
                case 5: return SERVER_ERROR;
                default: return OTHER;
            }
        }
    }

    public static final StatusCode OK = StatusCode.from(200);
    public static final StatusCode NO_CONTENT = StatusCode.from(204);
    public static final StatusCode CONFLICT = StatusCode.from(409);
    public static final StatusCode TOO_MANY_REQUESTS = StatusCode.from(429);

    private final int value;

    public static StatusCode from(int value) {
        return new StatusCode(value);
    }

    public StatusCode(int value) {
        this.value = value;
    }

    public boolean is(Family family) {
        return this.family() == family;
    }

    public boolean isOneOf(Family first, Family ... rest) {
        return isOneOf(EnumSet.of(first, rest));
    }

    public boolean isOneOf(Set<Family> families) {
        return families.contains(this.family());
    }

    public Family family() {
        return Family.of(value);
    }

    public int value() {
        return this.value;
    }

    @Override
    public String toString() {
        return "status code " + value + " (" + family() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StatusCode && this.value == ((StatusCode) obj).value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

}
