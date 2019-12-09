package no.digipost.signature.client.archive;

import no.digipost.signature.client.core.WithOrganizationNumber;

public final class ArchiveOwner implements WithOrganizationNumber {

    public static ArchiveOwner of(WithOrganizationNumber organization) {
        return ofOrganizationNumber(organization.getOrganizationNumber());
    }

    public static ArchiveOwner ofOrganizationNumber(String organizationNumber) {
        return new ArchiveOwner(organizationNumber);
    }


    private final String organizationNumber;

    private ArchiveOwner(String organizationNumber) {
        this.organizationNumber = organizationNumber;
    }

    @Override
    public String getOrganizationNumber() {
        return organizationNumber;
    }

}
