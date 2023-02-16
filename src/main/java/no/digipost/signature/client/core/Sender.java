package no.digipost.signature.client.core;

import java.util.Objects;

public final class Sender implements WithOrganizationNumber {

    private final String organizationNumber;
    private final PollingQueue pollingQueue;

    public Sender(String organizationNumber) {
        this(organizationNumber, PollingQueue.DEFAULT);
    }

    public Sender(String organizationNumber, PollingQueue pollingQueue) {
        this.organizationNumber = organizationNumber;
        this.pollingQueue = pollingQueue;
    }

    @Override
    public String getOrganizationNumber() {
        return organizationNumber;
    }

    public PollingQueue getPollingQueue() {
        return pollingQueue;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Sender) {
            Sender that = (Sender) other;
            return Objects.equals(this.organizationNumber, that.organizationNumber) &&
                    Objects.equals(this.pollingQueue, that.pollingQueue);
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(organizationNumber, pollingQueue);
    }

    @Override
    public String toString() {
        return "sender " + organizationNumber + ", " + pollingQueue;
    }

}
