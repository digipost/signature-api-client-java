package no.digipost.signature.client.asice.signature;

import no.digipost.signature.client.asice.ASiCEAttachable;
import no.digipost.signature.client.core.exceptions.ConfigurationException;
import no.digipost.signature.client.core.exceptions.RuntimeIOException;
import no.digipost.signature.client.core.exceptions.XmlConfigurationException;
import no.digipost.signature.client.core.exceptions.XmlValidationException;
import no.digipost.signature.client.security.KeyStoreConfig;
import no.digipost.signature.xsd.SignatureApiSchemas;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.validation.SchemaLoaderUtils;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.apache.commons.codec.digest.DigestUtils.sha256;

@SuppressWarnings("FieldCanBeLocal")
public class CreateSignature {

    public static final String C14V1 = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";

    private final String asicNamespace = "http://uri.etsi.org/2918/v1.2.1#";
    private final String signedPropertiesType = "http://uri.etsi.org/01903#SignedProperties";


    private final DigestMethod sha256DigestMethod;
    private final CanonicalizationMethod canonicalizationMethod;
    private final Transform canonicalXmlTransform;
    private final DocumentBuilderFactory documentBuilderFactory;

    private final CreateXAdESArtifacts createXAdESArtifacts;
    private final TransformerFactory transformerFactory;
    private final Schema schema;


    public CreateSignature(Clock clock) {

        createXAdESArtifacts = new CreateXAdESArtifacts(clock);

        transformerFactory = TransformerFactory.newInstance();
        try {
            XMLSignatureFactory xmlSignatureFactory = getSignatureFactory();
            sha256DigestMethod = xmlSignatureFactory.newDigestMethod(DigestMethod.SHA256, null);
            canonicalizationMethod = xmlSignatureFactory.newCanonicalizationMethod(C14V1, (C14NMethodParameterSpec) null);
            canonicalXmlTransform = xmlSignatureFactory.newTransform(C14V1, (TransformParameterSpec) null);
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new ConfigurationException("Failed to initialize XML-signing", e);
        }

        schema = loadSchema();
    }

    private Schema loadSchema() {
        try {
            return SchemaLoaderUtils.loadSchema(new Resource[]{new ClassPathResource(SignatureApiSchemas.XMLDSIG_SCHEMA), new ClassPathResource(SignatureApiSchemas.ASICE_SCHEMA)}, XmlValidatorFactory.SCHEMA_W3C_XML);
        } catch (IOException | SAXException e) {
            throw new ConfigurationException("Failed to load schemas for validating signatures", e);
        }
    }

    public Signature createSignature(final List<ASiCEAttachable> attachedFiles, final KeyStoreConfig keyStoreConfig) {
        XMLSignatureFactory xmlSignatureFactory = getSignatureFactory();
        SignatureMethod signatureMethod = getSignatureMethod(xmlSignatureFactory);

        // Generate XAdES document to sign, information about the key used for signing and information about what's signed
        XAdESArtifacts xadesArtifacts = createXAdESArtifacts.createArtifactsToSign(attachedFiles, keyStoreConfig.getCertificate());

        // Create signature references for all files
        List<Reference> references = references(xmlSignatureFactory, attachedFiles);

        // Create signature reference for XAdES properties
        references.add(xmlSignatureFactory.newReference(
                xadesArtifacts.signerPropertiesReferenceUri,
                sha256DigestMethod,
                singletonList(canonicalXmlTransform),
                signedPropertiesType,
                null
                ));

        KeyInfo keyInfo = keyInfo(xmlSignatureFactory, keyStoreConfig.getCertificateChain());
        SignedInfo signedInfo = xmlSignatureFactory.newSignedInfo(canonicalizationMethod, signatureMethod, references);

        // Define signature over XAdES document
        XMLObject xmlObject = xmlSignatureFactory.newXMLObject(singletonList(new DOMStructure(xadesArtifacts.document.getDocumentElement())), null, null, null);
        XMLSignature xmlSignature = xmlSignatureFactory.newXMLSignature(signedInfo, keyInfo, singletonList(xmlObject), "Signature", null);
        Document signedDocument = newEmptyXmlDocument();
        DOMSignContext signContext = new DOMSignContext(keyStoreConfig.getPrivateKey(), signedDocument);
        URIDereferencer defaultUriDereferencer = xmlSignatureFactory.getURIDereferencer();
        signContext.setURIDereferencer((uriReference, context) -> {
            if (xadesArtifacts.signerPropertiesReferenceUri.equals(uriReference.getURI())) {
                return (NodeSetData) () -> singleton(xadesArtifacts.signableProperties).iterator();
            }
            return defaultUriDereferencer.dereference(uriReference, context);
        });
        try {
            xmlSignature.sign(signContext);
        } catch (MarshalException e) {
            throw new XmlConfigurationException("failed to read ASiC-E XML for signing", e);
        } catch (XMLSignatureException e) {
            throw new XmlConfigurationException("Failed to sign ASiC-E element.", e);
        }

        wrapSignatureInXADeSEnvelope(signedDocument);

        ByteArrayOutputStream outputStream;
        try {
            outputStream = new ByteArrayOutputStream();
            Transformer transformer = transformerFactory.newTransformer();
            schema.newValidator().validate(new DOMSource(signedDocument));
            transformer.transform(new DOMSource(signedDocument), new StreamResult(outputStream));
        } catch (TransformerException e) {
            throw new ConfigurationException("Unable to serialize XML.", e);
        } catch (SAXException e) {
            throw new XmlValidationException("Failed to validate generated signature.xml. Verify that the input is valid and that there are no illegal symbols in file names etc.", e);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
        return new Signature(outputStream.toByteArray());
    }

    private Document newEmptyXmlDocument() {
        try {
            return documentBuilderFactory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new XmlConfigurationException("Unable to create new Document. " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
        }
    }

    private SignatureMethod getSignatureMethod(final XMLSignatureFactory xmlSignatureFactory) {
        try {
            return xmlSignatureFactory.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new ConfigurationException("Failed to initialize XML-signing", e);
        }
    }

    private List<Reference> references(final XMLSignatureFactory xmlSignatureFactory, final List<ASiCEAttachable> files) {
        List<Reference> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            try {
                String signatureElementId = "ID_" + i;
                String uri = URLEncoder.encode(files.get(i).getFileName(), "UTF-8");
                Reference reference = xmlSignatureFactory.newReference(uri, sha256DigestMethod, null, null, signatureElementId, sha256(files.get(i).getBytes()));
                result.add(reference);
            } catch(UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private KeyInfo keyInfo(final XMLSignatureFactory xmlSignatureFactory, final Certificate[] sertifikater) {
        KeyInfoFactory keyInfoFactory = xmlSignatureFactory.getKeyInfoFactory();
        X509Data x509Data = keyInfoFactory.newX509Data(asList(sertifikater));
        return keyInfoFactory.newKeyInfo(singletonList(x509Data));
    }

    private void wrapSignatureInXADeSEnvelope(final Document document) {
        Node signatureElement = document.removeChild(document.getDocumentElement());
        Element xadesElement = document.createElementNS(asicNamespace, "XAdESSignatures");
        xadesElement.appendChild(signatureElement);
        document.appendChild(xadesElement);
    }


    private XMLSignatureFactory getSignatureFactory() {
        try {
            return XMLSignatureFactory.getInstance("DOM", "XMLDSig");
        } catch (NoSuchProviderException e) {
            throw new ConfigurationException("Failed to find XML Digital Signature provided. The library depends on default Java-provider");
        }
    }
}
