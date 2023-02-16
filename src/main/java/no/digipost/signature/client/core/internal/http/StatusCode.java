package no.digipost.signature.client.core.internal.http;

import java.util.EnumSet;
import java.util.Set;

public final class StatusCode {

    private final int value;

    public static StatusCode from(int value) {
        return new StatusCode(value);
    }

    public StatusCode(int value) {
        this.value = value;
    }

    public boolean is(StatusCodeFamily family) {
        return this.family() == family;
    }

    public boolean isOneOf(StatusCodeFamily first, StatusCodeFamily ... rest) {
        return isOneOf(EnumSet.of(first, rest));
    }

    public boolean isOneOf(Set<StatusCodeFamily> families) {
        return families.contains(this.family());
    }

    public StatusCodeFamily family() {
        return StatusCodeFamily.of(value);
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
