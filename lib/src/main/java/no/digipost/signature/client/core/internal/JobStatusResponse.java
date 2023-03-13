package no.digipost.signature.client.core.internal;

import java.time.Instant;

public class JobStatusResponse<JOB_STATUS> {

    private final JOB_STATUS status;
    private final Instant nextPermittedPollTime;

    JobStatusResponse(JOB_STATUS status, Instant nextPermittedPollTime) {
        this.status = status;
        this.nextPermittedPollTime = nextPermittedPollTime;
    }

    public JOB_STATUS getStatusResponse() {
        return status;
    }

    public boolean gotStatusChange() {
        return status != null;
    }

    public Instant getNextPermittedPollTime() {
        return nextPermittedPollTime;
    }
}
