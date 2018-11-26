package no.digipost.signature.client.asice.signature;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static java.util.stream.IntStream.range;

final class XAdESArtifacts {

    public static XAdESArtifacts from(Document document) {
        Element qualifyingProperties = document.getDocumentElement();
        NodeList qualifyingPropertiesContents = qualifyingProperties.getChildNodes();
        Element signedProperties = range(0, qualifyingPropertiesContents.getLength()).mapToObj(qualifyingPropertiesContents::item)
            .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
            .map(Element.class::cast)
            .filter(element -> "SignedProperties".equals(element.getLocalName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Didn't find SignedProperties in document."));
        String signerPropertiesReferenceUri = signedProperties.getAttribute("Id");
        return new XAdESArtifacts(document, signedProperties, "#" + signerPropertiesReferenceUri);
    }


    public final Document document;
    public final Element signableProperties;
    public final String signerPropertiesReferenceUri;

    private XAdESArtifacts(Document document, Element signableProperties, String signerPropertiesReferenceUri) {
        this.document = document;
        this.signableProperties = signableProperties;
        this.signerPropertiesReferenceUri = signerPropertiesReferenceUri;
    }

}
