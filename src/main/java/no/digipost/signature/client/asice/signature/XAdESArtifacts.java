package no.digipost.signature.client.asice.signature;

import no.digipost.signature.api.xml.thirdparty.xades.QualifyingProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMResult;

import static java.util.stream.IntStream.range;

final class XAdESArtifacts {

    private static Marshaller marshaller;

    static {
        try {
            marshaller = JAXBContext.newInstance(QualifyingProperties.class).createMarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }


    public static XAdESArtifacts from(QualifyingProperties qualifyingProperties) {
        DOMResult domResult = new DOMResult();
        try {
            marshaller.marshal(qualifyingProperties, domResult);
        } catch (JAXBException e) {
            throw new RuntimeException(
                    "Failed to marshal qualifying properties, because " +
                    e.getClass().getSimpleName() + " '" + e.getMessage() + "'", e);
        }
        return from((Document) domResult.getNode());
    }

    private static XAdESArtifacts from(Document qualifyingPropertiesDocument) {
        Element qualifyingProperties = qualifyingPropertiesDocument.getDocumentElement();
        NodeList qualifyingPropertiesContents = qualifyingProperties.getChildNodes();
        Element signedProperties = range(0, qualifyingPropertiesContents.getLength()).mapToObj(qualifyingPropertiesContents::item)
            .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
            .map(Element.class::cast)
            .filter(element -> "SignedProperties".equals(element.getLocalName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Didn't find SignedProperties in document."));
        String signerPropertiesReferenceUri = signedProperties.getAttribute("Id");
        return new XAdESArtifacts(qualifyingPropertiesDocument, signedProperties, "#" + signerPropertiesReferenceUri);
    }


    public final Document document;
    public final Element signableProperties;
    public final String signablePropertiesReferenceUri;

    private XAdESArtifacts(Document document, Element signableProperties, String signerPropertiesReferenceUri) {
        this.document = document;
        this.signableProperties = signableProperties;
        this.signablePropertiesReferenceUri = signerPropertiesReferenceUri;
    }

}
