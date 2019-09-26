package no.digipost.signature.client.archive;

import no.digipost.signature.client.core.WithOrganizationNumber;

public final class DocumentOwner implements WithOrganizationNumber {

    public static DocumentOwner of(WithOrganizationNumber organization) {
        return ofOrganizationNumber(organization.getOrganizationNumber());
    }

    public static DocumentOwner ofOrganizationNumber(String organizationNumber) {
        return new DocumentOwner(organizationNumber);
    }


    private final String organizationNumber;

    private DocumentOwner(String organizationNumber) {
        this.organizationNumber = organizationNumber;
    }

    @Override
    public String getOrganizationNumber() {
        return organizationNumber;
    }

}
