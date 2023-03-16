package no.digipost.signature.client.asice.signature;

import no.digipost.signature.api.xml.thirdparty.asice.XAdESSignatures;
import no.digipost.signature.api.xml.thirdparty.xades.DataObjectFormat;
import no.digipost.signature.api.xml.thirdparty.xades.DigestAlgAndValueType;
import no.digipost.signature.api.xml.thirdparty.xades.QualifyingProperties;
import no.digipost.signature.api.xml.thirdparty.xades.SignedDataObjectProperties;
import no.digipost.signature.api.xml.thirdparty.xades.SigningCertificate;
import no.digipost.signature.api.xml.thirdparty.xmldsig.Reference;
import no.digipost.signature.api.xml.thirdparty.xmldsig.SignedInfo;
import no.digipost.signature.api.xml.thirdparty.xmldsig.X509IssuerSerialType;
import no.digipost.signature.client.TestKonfigurasjon;
import no.digipost.signature.client.asice.ASiCEAttachable;
import no.digipost.signature.client.core.DocumentType;
import no.digipost.signature.client.security.KeyStoreConfig;
import no.digipost.signature.jaxb.JaxbMarshaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class CreateSignatureTest {

    private CreateSignature createSignature;

    /**
     * SHA256 hash of "hoveddokument-innhold"
     */
    private final byte[] expectedDokumentHash = new byte[] { 93, -36, 99, 92, -27, 39, 21, 31, 33, -127, 30, 77, 6, 49, 92, -48, -114, -61, -100, -126, -64, -70, 70, -38, 67, 93, -126, 62, -125, -7, -115, 123 };

    private KeyStoreConfig noekkelpar;
    private List<ASiCEAttachable> files;

    private static final JaxbMarshaller unmarshaller = new JaxbMarshaller(new HashSet<>(asList(XAdESSignatures.class, QualifyingProperties.class)));

    @BeforeEach
    void setUp() {
        noekkelpar = TestKonfigurasjon.CLIENT_KEYSTORE;
        files = asList(
                file("dokument.pdf", "hoveddokument-innhold".getBytes(), DocumentType.PDF.getMediaType()),
                file("manifest.xml", "manifest-innhold".getBytes(), ASiCEAttachable.XML_MEDIATYPE)
        );

        ZonedDateTime signingTime = ZonedDateTime.of(2018, 11, 29, 9, 15, 0, 0, ZoneId.of("Europe/Oslo"));
        createSignature = new CreateSignature(Clock.fixed(signingTime.toInstant(), signingTime.getZone()));
    }

    @Test
    void test_generated_signatures() {
        /*
         * Expected signature value (Base-64 encoded) from the given keys, files, and time of the signing.
         * If this changes, something has changed in the signature implementation, and must be investigated!
         */
        final String expectedBase64EncodedSignatureValue =
                "L3HFN44OGUEbK5p9zxbnDZrey+UnQ9fYQX0k7gv8hfxouRfXFvNHXtUJEI00/BOlhcyGRu8wEIpKYEkDIzj" +
                "WZyKXjV8Tz6PkHMJedgVGFDPlKwkx7gufntbjH2xqhBOsJzobDQ44rqOlK1YiXNQCPAMFwN/CpOTQTRFuf9" +
                "/37BN2QG5cmgz+ZNqcKPwQnjrVaQBOrQEc5D2/n05aPsRdc6OUzu2TIftoRLRH1peRDLCAo7MjPNhYo1CCi" +
                "nBa0FMipG5jtqUPYJJMTt56wIJlwQ95PhGIEYtdRYwxlgSau9Bw+wYmD4NU0K0hw6FgBQ/UDF87T5Zr7HTPWPMwpngkWg==";

        Signature signature = createSignature.createSignature(files, noekkelpar);
        XAdESSignatures xAdESSignatures = unmarshaller.unmarshal(signature.getContent(), XAdESSignatures.class);

        assertThat(xAdESSignatures, where(XAdESSignatures::getSignatures, hasSize(1)));
        no.digipost.signature.api.xml.thirdparty.xmldsig.Signature dSignature = xAdESSignatures.getSignatures().get(0);
        verify_signed_info(dSignature.getSignedInfo());
        assertThat("signature value", dSignature.getSignatureValue().getValue(), where(Base64.getEncoder()::encodeToString, is(expectedBase64EncodedSignatureValue)));
        assertThat(dSignature.getKeyInfo(), is(notNullValue()));
    }

    @Test
    void test_xades_signed_properties() {
        Signature signature = createSignature.createSignature(files, noekkelpar);

        XAdESSignatures xAdESSignatures = unmarshaller.unmarshal(signature.getContent(), XAdESSignatures.class);
        no.digipost.signature.api.xml.thirdparty.xmldsig.Object object = xAdESSignatures.getSignatures().get(0).getObjects().get(0);

        QualifyingProperties xadesProperties = (QualifyingProperties) object.getContent().get(0);
        SigningCertificate signingCertificate = xadesProperties.getSignedProperties().getSignedSignatureProperties().getSigningCertificate();
        verify_signing_certificate(signingCertificate);

        SignedDataObjectProperties signedDataObjectProperties = xadesProperties.getSignedProperties().getSignedDataObjectProperties();
        verify_signed_data_object_properties(signedDataObjectProperties);
    }

    @Test
    void should_support_filenames_with_spaces_and_other_characters() {
        List<ASiCEAttachable> otherFiles = asList(
                file("dokument (2).pdf", "hoveddokument-innhold".getBytes(), DocumentType.PDF.getMediaType()),
                file("manifest.xml", "manifest-innhold".getBytes(), ASiCEAttachable.XML_MEDIATYPE)
        );

        Signature signature = createSignature.createSignature(otherFiles, noekkelpar);
        XAdESSignatures xAdESSignatures = unmarshaller.unmarshal(signature.getContent(), XAdESSignatures.class);
        String uri = xAdESSignatures.getSignatures().get(0).getSignedInfo().getReferences().get(0).getURI();
        assertThat(uri, is("dokument+%282%29.pdf"));
    }

    private void verify_signed_data_object_properties(final SignedDataObjectProperties signedDataObjectProperties) {
        assertThat(signedDataObjectProperties.getDataObjectFormats(), hasSize(2)); // One per file
        DataObjectFormat dokumentDataObjectFormat = signedDataObjectProperties.getDataObjectFormats().get(0);
        assertThat(dokumentDataObjectFormat.getObjectReference(), is("#ID_0"));
        assertThat(dokumentDataObjectFormat.getMimeType(), is("application/pdf"));

        DataObjectFormat manifestDataObjectFormat = signedDataObjectProperties.getDataObjectFormats().get(1);
        assertThat(manifestDataObjectFormat.getObjectReference(), is("#ID_1"));
        assertThat(manifestDataObjectFormat.getMimeType(), is("application/xml"));
    }

    private void verify_signing_certificate(final SigningCertificate signingCertificate) {
        assertThat(signingCertificate.getCerts(), hasSize(1));

        DigestAlgAndValueType certDigest = signingCertificate.getCerts().get(0).getCertDigest();
        assertThat(certDigest.getDigestMethod().getAlgorithm(), is("http://www.w3.org/2000/09/xmldsig#sha1"));
        assertThat(certDigest.getDigestValue().length, is(20)); // SHA1 is 160 bits => 20 bytes

        X509IssuerSerialType issuerSerial = signingCertificate.getCerts().get(0).getIssuerSerial();
        assertThat(issuerSerial.getX509IssuerName(), is("CN=Avsender,OU=Avsender,O=Avsender,L=Oslo,ST=NO,C=NO"));
        assertThat(issuerSerial.getX509SerialNumber(), is(new BigInteger("589725471")));
    }

    private void verify_signed_info(final SignedInfo signedInfo) {
        assertThat(signedInfo.getCanonicalizationMethod().getAlgorithm(), is("http://www.w3.org/TR/2001/REC-xml-c14n-20010315"));
        assertThat(signedInfo.getSignatureMethod().getAlgorithm(), is("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"));

        List<Reference> references = signedInfo.getReferences();
        assertThat(references, hasSize(3));
        assert_dokument_reference(references.get(0));
        assertThat(references.get(1).getURI(), is("manifest.xml"));
        verify_signed_properties_reference(references.get(2));
    }

    private void verify_signed_properties_reference(final Reference signedPropertiesReference) {
        assertThat(signedPropertiesReference.getURI(), is("#SignedProperties"));
        assertThat(signedPropertiesReference.getType(), is("http://uri.etsi.org/01903#SignedProperties"));
        assertThat(signedPropertiesReference.getDigestMethod().getAlgorithm(), is("http://www.w3.org/2001/04/xmlenc#sha256"));
        assertThat(signedPropertiesReference.getDigestValue().length, is(32)); // SHA256 is 256 bits => 32 bytes
        assertThat(signedPropertiesReference.getTransforms().getTransforms().get(0).getAlgorithm(), is("http://www.w3.org/TR/2001/REC-xml-c14n-20010315"));
    }

    private void assert_dokument_reference(final Reference dokumentReference) {
        assertThat(dokumentReference.getURI(), is("dokument.pdf"));
        assertThat(dokumentReference.getDigestValue(), is(expectedDokumentHash));
        assertThat(dokumentReference.getDigestMethod().getAlgorithm(), is("http://www.w3.org/2001/04/xmlenc#sha256"));
    }

    private ASiCEAttachable file(String fileName, byte[] contents, String mediaType) {
        return new ASiCEAttachable() {
            @Override
            public String getFileName() { return fileName; }
            @Override
            public byte[] getContent() { return contents; }
            @Override
            public String getMediaType() { return mediaType; }
        };
    }

}
