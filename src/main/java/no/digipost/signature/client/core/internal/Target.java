package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.Sender;

import static java.lang.String.format;

enum Target {
    PORTAL("/%s/portal/signature-jobs"),
    DIRECT("/%s/direct/signature-jobs");

    private final String path;

    Target(String path) {
        this.path = path;
    }

    String path(Sender sender) {
        return format(this.path, sender.getOrganizationNumber());
    }
}
