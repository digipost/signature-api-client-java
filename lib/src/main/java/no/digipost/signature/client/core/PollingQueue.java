package no.digipost.signature.client.core;

import java.util.Objects;

public final class PollingQueue {

    public final static PollingQueue DEFAULT = new PollingQueue(null);

    public final String value;

    private PollingQueue(String value) {
        this.value = value;
    }

    public static PollingQueue of(String value) {
        return new PollingQueue(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PollingQueue) {
            PollingQueue that = (PollingQueue) obj;
            return Objects.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value != null ? "polling-queue '" + value + "'" : "no specified polling-queue (default)";
    }

}
