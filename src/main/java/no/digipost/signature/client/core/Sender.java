package no.digipost.signature.client.core;

public class Sender {

    private final String organizationNumber;
    private final PollingQueue pollingQueue;

    public Sender(String organizationNumber) {
        this(organizationNumber, PollingQueue.DEFAULT);
    }

    public Sender(String organizationNumber, PollingQueue pollingQueue) {
        this.organizationNumber = organizationNumber;
        this.pollingQueue = pollingQueue;
    }

    public String getOrganizationNumber() {
        return organizationNumber;
    }

    public PollingQueue getPollingQueue() {
        return pollingQueue;
    }
}
