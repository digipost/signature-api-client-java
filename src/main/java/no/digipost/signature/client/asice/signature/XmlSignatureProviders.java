package no.digipost.signature.client.asice.signature;

import javax.xml.crypto.dsig.XMLSignatureFactory;

import java.security.Provider;
import java.security.Security;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

final class XmlSignatureProviders {

    public static final Provider DOM_XML_SIGNATURE = Optional.ofNullable(Security.getProviders("XMLSignatureFactory.DOM"))
            .map(Stream::of).orElseGet(Stream::empty)
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Java Security Provider for DOM-capable XML signature (XMLSignatureFactory.DOM) not available"));

    public static XMLSignatureFactory getSignatureFactory() {
        return XMLSignatureFactory.getInstance("DOM", DOM_XML_SIGNATURE);
    }

    private XmlSignatureProviders() {
    }

}