package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.api.xml.XMLDirectDocument;
import no.digipost.signature.api.xml.XMLDirectSignatureJobManifest;
import no.digipost.signature.api.xml.XMLDirectSigner;
import no.digipost.signature.api.xml.XMLHref;
import no.digipost.signature.api.xml.XMLSender;
import no.digipost.signature.client.core.AuthenticationLevel;
import no.digipost.signature.client.core.IdentifierInSignedDocuments;
import no.digipost.signature.client.core.OnBehalfOf;
import no.digipost.signature.client.core.SignatureType;
import no.digipost.signature.client.core.internal.MaySpecifySender;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.direct.DirectSigner;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class CreateDirectManifest extends ManifestCreator<DirectJob> {

    private final MaySpecifySender defaultSenderConfiguration;

    public CreateDirectManifest(MaySpecifySender defaultSenderConfiguration) {
        this.defaultSenderConfiguration = defaultSenderConfiguration;
    }

    @Override
    XMLDirectSignatureJobManifest buildXmlManifest(DirectJob job) {
        List<XMLDirectSigner> signers = new ArrayList<>();
        for (DirectSigner signer : job.getSigners()) {
            XMLDirectSigner xmlSigner = new XMLDirectSigner()
                    .withSignatureType(signer.getSignatureType().map(SignatureType::getXmlEnumValue).orElse(null))
                    .withOnBehalfOf(signer.getOnBehalfOf().map(OnBehalfOf::getXmlEnumValue).orElse(null));
            if (signer.isIdentifiedByPersonalIdentificationNumber()) {
                xmlSigner.setPersonalIdentificationNumber(signer.getPersonalIdentificationNumber());
            } else {
                xmlSigner.setSignerIdentifier(signer.getCustomIdentifier());
            }
            signers.add(xmlSigner);
        }

        return new XMLDirectSignatureJobManifest()
                .withSigners(signers)
                .withRequiredAuthentication(job.getRequiredAuthentication().map(AuthenticationLevel::getXmlEnumValue).orElse(null))
                .withSender(new XMLSender().withOrganizationNumber(job.resolveSenderWithFallbackTo(defaultSenderConfiguration).getOrganizationNumber()))
                .withTitle(job.getTitle())
                .withDescription(job.getDescription().orElse(null))
                .withDocuments(job.getDocuments().stream()
                        .map(document -> new XMLDirectDocument()
                                    .withTitle(document.getTitle())
                                    .withHref(XMLHref.of(document.getFileName()))
                                    .withMediaType(document.getMediaType()))
                        .collect(toList()))
                .withIdentifierInSignedDocuments(job.getIdentifierInSignedDocuments().map(IdentifierInSignedDocuments::getXmlEnumValue).orElse(null));
    }
}
