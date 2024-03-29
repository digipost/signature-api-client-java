package no.digipost.signature.client.asice.signature;

import no.digipost.signature.client.core.exceptions.ConfigurationException;
import no.digipost.signature.client.core.exceptions.XmlConfigurationException;
import no.digipost.signature.client.core.exceptions.XmlValidationException;
import no.digipost.signature.client.security.KeyStoreConfig;
import no.digipost.signature.jaxb.JaxbMarshaller;
import no.digipost.signature.xsd.SignatureApiSchemas;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
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
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public class CreateSignature {

    private static final String C14V1 = CanonicalizationMethod.INCLUSIVE;
    private static final String ASIC_NAMESPACE = "http://uri.etsi.org/2918/v1.2.1#";
    private static final String SIGNED_PROPERTIES_TYPE = "http://uri.etsi.org/01903#SignedProperties";


    private final DigestMethod sha256DigestMethod;
    private final CanonicalizationMethod canonicalizationMethod;
    private final Transform canonicalXmlTransform;

    private final DomUtils domUtils;
    private final CreateXAdESArtifacts createXAdESArtifacts;
    private final Schema schema;


    public CreateSignature(Clock clock) {

        domUtils = new DomUtils();
        createXAdESArtifacts = new CreateXAdESArtifacts(clock);

        XMLSignatureFactory xmlSignatureFactory = XmlSignatureProviders.getSignatureFactory();
        try {
            sha256DigestMethod = xmlSignatureFactory.newDigestMethod(DigestMethod.SHA256, null);
            canonicalizationMethod = xmlSignatureFactory.newCanonicalizationMethod(C14V1, (C14NMethodParameterSpec) null);
            canonicalXmlTransform = xmlSignatureFactory.newTransform(C14V1, (TransformParameterSpec) null);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new ConfigurationException("Failed to initialize XML-signing", e);
        }

        schema = loadSchema();
    }

    private static InputSource createInputSource(String resource) {
        URL resourceUrl = requireNonNull(JaxbMarshaller.class.getResource(resource), resource);
        try (InputStream inputStream = resourceUrl.openStream()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            final int bufLen = 1024;
            byte[] buf = new byte[bufLen];
            int readLen;
            while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                outputStream.write(buf, 0, readLen);

            InputSource source = new InputSource(new ByteArrayInputStream(outputStream.toByteArray()));
            source.setSystemId(resourceUrl.toString());
            return source;
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to resolve " + resource + " from " + resourceUrl + ", " +
                            "because " + e.getClass().getSimpleName() + " '" + e.getMessage() + "'", e);
        }
    }

    private static Schema createSchema(Collection<String> resources) {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            parserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

            SAXParser saxParser = parserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            Source[] schemaSources = resources.stream()
                    .map(resource -> new SAXSource(xmlReader, createInputSource(resource)))
                    .toArray(Source[]::new);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return schemaFactory.newSchema(schemaSources);
        } catch (Exception e) {
            throw new RuntimeException("Could not create schema from resources [" + String.join(", ", resources) + "]", e);
        }
    }

    private Schema loadSchema() {
        return createSchema(new HashSet<>(Arrays.asList(SignatureApiSchemas.XMLDSIG_SCHEMA, SignatureApiSchemas.ASICE_SCHEMA)));
    }

    public Signature createSignature(final List<? extends SignableFileReference> attachedFiles, final KeyStoreConfig keyStoreConfig) {
        return new Signature(domUtils.serializeToXml(createXmlSignature(attachedFiles, keyStoreConfig)));
    }

    protected Document createXmlSignature(final List<? extends SignableFileReference> attachedFiles, final KeyStoreConfig keyStoreConfig) {
        XMLSignatureFactory xmlSignatureFactory = XmlSignatureProviders.getSignatureFactory();
        SignatureMethod signatureMethod = getSignatureMethod(xmlSignatureFactory);

        // Generate XAdES document to sign, information about the key used for signing and information about what's signed
        XAdESArtifacts xadesArtifacts = createXAdESArtifacts.createArtifactsToSign(attachedFiles, keyStoreConfig.getCertificate());

        // Create signature references for all files
        List<Reference> references = references(xmlSignatureFactory, attachedFiles);

        // Create signature reference for XAdES properties
        references.add(xmlSignatureFactory.newReference(
                xadesArtifacts.signablePropertiesReferenceUri,
                sha256DigestMethod,
                singletonList(canonicalXmlTransform),
                SIGNED_PROPERTIES_TYPE,
                null
                ));

        KeyInfo keyInfo = keyInfo(xmlSignatureFactory, keyStoreConfig.getCertificateChain());
        SignedInfo signedInfo = xmlSignatureFactory.newSignedInfo(canonicalizationMethod, signatureMethod, references);

        // Define signature over XAdES document
        XMLObject xmlObject = xmlSignatureFactory.newXMLObject(singletonList(new DOMStructure(xadesArtifacts.document.getDocumentElement())), null, null, null);
        XMLSignature xmlSignature = xmlSignatureFactory.newXMLSignature(signedInfo, keyInfo, singletonList(xmlObject), "Signature", null);

        Document signedDocument = domUtils.newEmptyXmlDocument();
        DOMSignContext signContext = new DOMSignContext(keyStoreConfig.getPrivateKey(), addXAdESSignaturesElement(signedDocument));
        signContext.setURIDereferencer(signedPropertiesURIDereferencer(xadesArtifacts, xmlSignatureFactory));

        try {
            xmlSignature.sign(signContext);
        } catch (MarshalException e) {
            throw new XmlConfigurationException("failed to read ASiC-E XML for signing", e);
        } catch (XMLSignatureException e) {
            throw new XmlConfigurationException("Failed to sign ASiC-E element.", e);
        }

        try {
            schema.newValidator().validate(new DOMSource(signedDocument));
        } catch (SAXException | IOException e) {
            throw new XmlValidationException(
                    "Failed to validate generated signature.xml because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'. " +
                    "Verify that the input is valid and that there are no illegal symbols in file names etc.", e);
        }

        return signedDocument;
    }

    private URIDereferencer signedPropertiesURIDereferencer(XAdESArtifacts xadesArtifacts, XMLSignatureFactory signatureFactory) {
        return (uriReference, context) -> {
            if (xadesArtifacts.signablePropertiesReferenceUri.equals(uriReference.getURI())) {
                return (NodeSetData) domUtils.allNodesBelow(xadesArtifacts.signableProperties)::iterator;
            }
            return signatureFactory.getURIDereferencer().dereference(uriReference, context);
        };
    }

    private static Element addXAdESSignaturesElement(Document doc) {
        return (Element) doc.appendChild(doc.createElementNS(ASIC_NAMESPACE, "XAdESSignatures"));
    }

    private static SignatureMethod getSignatureMethod(final XMLSignatureFactory xmlSignatureFactory) {
        try {
            return xmlSignatureFactory.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new ConfigurationException("Failed to initialize XML-signing", e);
        }
    }

    private List<Reference> references(final XMLSignatureFactory xmlSignatureFactory, final List<? extends SignableFileReference> files) {
        List<Reference> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            try {
                String signatureElementId = "ID_" + i;
                SignableFileReference file = files.get(i);
                String uri = URLEncoder.encode(file.getFileName(), "UTF-8");
                Reference reference = xmlSignatureFactory.newReference(uri, sha256DigestMethod, null, null, signatureElementId, file.getSha256());
                result.add(reference);
            } catch(UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private static KeyInfo keyInfo(final XMLSignatureFactory xmlSignatureFactory, final Certificate[] sertifikater) {
        KeyInfoFactory keyInfoFactory = xmlSignatureFactory.getKeyInfoFactory();
        X509Data x509Data = keyInfoFactory.newX509Data(asList(sertifikater));
        return keyInfoFactory.newKeyInfo(singletonList(x509Data));
    }

}
