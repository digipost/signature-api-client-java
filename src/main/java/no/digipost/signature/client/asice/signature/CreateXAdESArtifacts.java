package no.digipost.signature.client.asice.signature;

import no.digipost.signature.api.xml.thirdparty.xades.CertIDType;
import no.digipost.signature.api.xml.thirdparty.xades.DataObjectFormat;
import no.digipost.signature.api.xml.thirdparty.xades.DigestAlgAndValueType;
import no.digipost.signature.api.xml.thirdparty.xades.QualifyingProperties;
import no.digipost.signature.api.xml.thirdparty.xades.SignedDataObjectProperties;
import no.digipost.signature.api.xml.thirdparty.xades.SignedProperties;
import no.digipost.signature.api.xml.thirdparty.xades.SignedSignatureProperties;
import no.digipost.signature.api.xml.thirdparty.xades.SigningCertificate;
import no.digipost.signature.api.xml.thirdparty.xmldsig.DigestMethod;
import no.digipost.signature.api.xml.thirdparty.xmldsig.X509IssuerSerialType;
import no.digipost.signature.client.core.exceptions.CertificateException;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.xml.crypto.dsig.DigestMethod.SHA1;
import static org.apache.commons.codec.digest.DigestUtils.sha1;

class CreateXAdESArtifacts {

    private final DigestMethod sha1DigestMethod = new DigestMethod(emptyList(), SHA1);
    private final Clock clock;


    CreateXAdESArtifacts(Clock clock) {
        this.clock = clock;
    }

    XAdESArtifacts createArtifactsToSign(List<? extends SignableFileReference> files, X509Certificate certificate) {
        byte[] certificateDigestValue;
        try {
            certificateDigestValue = sha1(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new CertificateException("Unable to get encoded from of certificate", e);
        }

        DigestAlgAndValueType certificateDigest = new DigestAlgAndValueType(sha1DigestMethod, certificateDigestValue);
        X509IssuerSerialType certificateIssuer = new X509IssuerSerialType(certificate.getIssuerDN().getName(), certificate.getSerialNumber());
        SigningCertificate signingCertificate = new SigningCertificate(singletonList(new CertIDType(certificateDigest, certificateIssuer, null)));

        ZonedDateTime now = ZonedDateTime.now(clock);
        SignedSignatureProperties signedSignatureProperties = new SignedSignatureProperties().withSigningTime(now).withSigningCertificate(signingCertificate);
        SignedDataObjectProperties signedDataObjectProperties = new SignedDataObjectProperties().withDataObjectFormats(dataObjectFormats(files));
        SignedProperties signedProperties = new SignedProperties(signedSignatureProperties, signedDataObjectProperties, "SignedProperties");
        QualifyingProperties qualifyingProperties = new QualifyingProperties().withSignedProperties(signedProperties).withTarget("#Signature");

        return XAdESArtifacts.from(qualifyingProperties);
    }

    private List<DataObjectFormat> dataObjectFormats(List<? extends SignableFileReference> files) {
        List<DataObjectFormat> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            String signatureElementIdReference = format("#ID_%s", i);
            result.add(new DataObjectFormat(null, null, files.get(i).getMimeType(), null, signatureElementIdReference));
        }
        return result;
    }

}
