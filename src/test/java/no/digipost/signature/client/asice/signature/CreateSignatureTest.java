/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.signature.client.asice.signature;

import no.digipost.signature.client.TestKonfigurasjon;
import no.digipost.signature.client.asice.ASiCEAttachable;
import no.digipost.signature.client.core.internal.xml.Marshalling;
import no.digipost.signature.client.security.KeyStoreConfig;
import no.digipost.signature.api.xml.thirdparty.asice.XAdESSignatures;
import no.digipost.signature.api.xml.thirdparty.xades.*;
import no.digipost.signature.api.xml.thirdparty.xmldsig.Reference;
import no.digipost.signature.api.xml.thirdparty.xmldsig.SignedInfo;
import no.digipost.signature.api.xml.thirdparty.xmldsig.X509IssuerSerialType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.transform.stream.StreamSource;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CreateSignatureTest {

    private CreateSignature createSignature;

    /**
     * SHA256 hash of "hoveddokument-innhold"
     */
    private final byte[] expectedDokumentHash = new byte[] { 93, -36, 99, 92, -27, 39, 21, 31, 33, -127, 30, 77, 6, 49, 92, -48, -114, -61, -100, -126, -64, -70, 70, -38, 67, 93, -126, 62, -125, -7, -115, 123 };

    private KeyStoreConfig noekkelpar;
    private List<ASiCEAttachable> files;

    private static final Jaxb2Marshaller marshaller = Marshalling.instance();

    @Before
    public void setUp() throws Exception {
        noekkelpar = TestKonfigurasjon.CLIENT_KEYSTORE;
        files = asList(
                file("dokument.pdf", "hoveddokument-innhold".getBytes(), "application/pdf"),
                file("manifest.xml", "manifest-innhold".getBytes(), "application/xml")
        );

        createSignature = new CreateSignature();
    }

    @Test
    public void test_generated_signatures() {
        Signature signature = createSignature.createSignature(files, noekkelpar);
        XAdESSignatures xAdESSignatures = (XAdESSignatures) marshaller.unmarshal(new StreamSource(new ByteArrayInputStream(signature.getBytes())));

        assertThat(xAdESSignatures.getSignatures(), hasSize(1));
        no.digipost.signature.api.xml.thirdparty.xmldsig.Signature dSignature = xAdESSignatures.getSignatures().get(0);
        verify_signed_info(dSignature.getSignedInfo());
        assertThat(dSignature.getSignatureValue(), is(notNullValue()));
        assertThat(dSignature.getKeyInfo(), is(notNullValue()));
    }

    @Test
    public void test_xades_signed_properties() {
        Signature signature = createSignature.createSignature(files, noekkelpar);

        XAdESSignatures xAdESSignatures = (XAdESSignatures) marshaller.unmarshal(new StreamSource(new ByteArrayInputStream(signature.getBytes())));
        no.digipost.signature.api.xml.thirdparty.xmldsig.Object object = xAdESSignatures.getSignatures().get(0).getObjects().get(0);

        QualifyingProperties xadesProperties = (QualifyingProperties) object.getContent().get(0);
        SigningCertificate signingCertificate = xadesProperties.getSignedProperties().getSignedSignatureProperties().getSigningCertificate();
        verify_signing_certificate(signingCertificate);

        SignedDataObjectProperties signedDataObjectProperties = xadesProperties.getSignedProperties().getSignedDataObjectProperties();
        verify_signed_data_object_properties(signedDataObjectProperties);
    }

    @Test
    public void should_support_filenames_with_spaces_and_other_characters() {
        List<ASiCEAttachable> otherFiles = asList(
                file("dokument (2).pdf", "hoveddokument-innhold".getBytes(), "application/pdf"),
                file("manifest.xml", "manifest-innhold".getBytes(), "application/xml")
        );

        Signature signature = createSignature.createSignature(otherFiles, noekkelpar);
        XAdESSignatures xAdESSignatures = (XAdESSignatures) marshaller.unmarshal(new StreamSource(new ByteArrayInputStream(signature.getBytes())));
        String uri = xAdESSignatures.getSignatures().get(0).getSignedInfo().getReferences().get(0).getURI();
        assertEquals("dokument+%282%29.pdf", uri);
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
        assertThat(issuerSerial.getX509IssuerName(), is("CN=Avsender, OU=Avsender, O=Avsender, L=Oslo, ST=NO, C=NO"));
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

    private ASiCEAttachable file(final String fileName, final byte[] contents, final String mimeType) {
        return new ASiCEAttachable() {
            @Override
            public String getFileName() { return fileName; }
            @Override
            public byte[] getBytes() { return contents; }
            @Override
            public String getMimeType() { return mimeType; }
        };
    }

}
