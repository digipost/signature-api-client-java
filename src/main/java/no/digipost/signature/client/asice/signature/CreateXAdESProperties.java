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

import no.digipost.signature.client.asice.ASiCEAttachable;
import no.digipost.signature.client.domain.exceptions.CertificateException;
import no.digipost.signature.client.domain.exceptions.XmlConfigurationException;
import org.etsi.uri._01903.v1_3.*;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.w3._2000._09.xmldsig_.DigestMethod;
import org.w3._2000._09.xmldsig_.X509IssuerSerialType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static javax.xml.crypto.dsig.DigestMethod.SHA1;
import static org.apache.commons.codec.digest.DigestUtils.sha1;

public class CreateXAdESProperties {

    private final DigestMethod sha1DigestMethod = new DigestMethod(emptyList(), SHA1);

    private static Jaxb2Marshaller marshaller;

    static {
        marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(QualifyingProperties.class);
    }

    public Document createPropertiesToSign(final List<ASiCEAttachable> files, final X509Certificate certificate) {
        byte[] certificateDigestValue;
        try {
            certificateDigestValue = sha1(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new CertificateException("Unable to get encoded from of certificate", e);
        }

        DigestAlgAndValueType certificateDigest = new DigestAlgAndValueType(sha1DigestMethod, certificateDigestValue);
        X509IssuerSerialType certificateIssuer = new X509IssuerSerialType(certificate.getIssuerDN().getName(), certificate.getSerialNumber());
        SigningCertificate signingCertificate = new SigningCertificate(singletonList(new CertIDType(certificateDigest, certificateIssuer, null)));

        Date now = new Date();
        SignedSignatureProperties signedSignatureProperties = new SignedSignatureProperties(now, signingCertificate, null, null, null, null);
        SignedDataObjectProperties signedDataObjectProperties = new SignedDataObjectProperties(dataObjectFormats(files), null, null, null, null);
        SignedProperties signedProperties = new SignedProperties(signedSignatureProperties, signedDataObjectProperties, "SignedProperties");
        QualifyingProperties qualifyingProperties = new QualifyingProperties(signedProperties, null, "#Signature", null);

        DOMResult domResult = new DOMResult();
        marshaller.marshal(qualifyingProperties, domResult);
        Document document = (Document) domResult.getNode();

        // Explicitly mark the SignedProperties Id as an Document ID attribute, so that it will be eligble as a reference for signature.
        // If not, it will not be treated as something to sign.
        markAsIdProperty(document, "SignedProperties", "Id");

        return document;
    }

    private List<DataObjectFormat> dataObjectFormats(final List<ASiCEAttachable> files) {
        List<DataObjectFormat> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            String signatureElementIdReference = format("#ID_%s", i);
            result.add(new DataObjectFormat(null, null, files.get(i).getMimeType(), null, signatureElementIdReference));
        }
        return result;
    }

    private void markAsIdProperty(final Document document, final String elementName, final String property) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            Element idElement = (Element) xPath.evaluate("//*[local-name()='" + elementName + "']", document, XPathConstants.NODE);
            idElement.setIdAttribute(property, true);

        } catch (XPathExpressionException e) {
            throw new XmlConfigurationException("XPath on generated XML failed.", e);
        }
    }
}
