package no.digipost.signature.client.asice.manifest;

import no.digipost.signature.api.xml.XMLDirectDocument;
import no.digipost.signature.api.xml.XMLDirectSignatureJobManifest;
import no.digipost.signature.api.xml.XMLDirectSigner;
import no.digipost.signature.api.xml.XMLSender;
import no.digipost.signature.client.core.AuthenticationLevel;
import no.digipost.signature.client.core.IdentifierInSignedDocuments;
import no.digipost.signature.client.core.OnBehalfOf;
import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.SignatureType;
import no.digipost.signature.client.direct.DirectDocument;
import no.digipost.signature.client.direct.DirectJob;
import no.digipost.signature.client.direct.DirectSigner;

import java.util.ArrayList;
import java.util.List;

public class CreateDirectManifest extends ManifestCreator<DirectJob> {

    @Override
    Object buildXmlManifest(DirectJob job, Sender sender) {
        DirectDocument document = job.getDocument();

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
                .withSender(new XMLSender().withOrganizationNumber(sender.getOrganizationNumber()))
                .withDocument(new XMLDirectDocument()
                        .withTitle(document.getTitle())
                        .withDescription(document.getMessage())
                        .withHref(document.getFileName())
                        .withMime(document.getMimeType())
                )
                .withIdentifierInSignedDocuments(job.getIdentifierInSignedDocuments().map(IdentifierInSignedDocuments::getXmlEnumValue).orElse(null))
                ;
    }
}
