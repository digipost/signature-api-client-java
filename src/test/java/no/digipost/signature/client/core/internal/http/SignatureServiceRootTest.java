package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.ServiceEnvironment;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.toList;
import static no.digipost.DiggExceptions.applyUnchecked;
import static no.digipost.signature.client.ServiceEnvironment.STAGING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class SignatureServiceRootTest {

    @Test
    void constructedFromAllServiceEnvironments() {
        List<ServiceEnvironment> allPredefinedEnvironments = Stream.of(ServiceEnvironment.class.getFields())
            .filter(f -> isStatic(f.getModifiers()) && isFinal(f.getModifiers()))
            .filter(staticFinalField -> ServiceEnvironment.class.isAssignableFrom(staticFinalField.getType()))
            .map(serviceEnvironmentField -> (ServiceEnvironment) applyUnchecked(serviceEnvironmentField::get, ServiceEnvironment.class))
            .collect(toList());
        assertThat(allPredefinedEnvironments, everyItem(where(SignatureServiceRoot::from, isA(SignatureServiceRoot.class))));
    }

    @Test
    void constructFromLocalhostUrl() {
        URI localhostUrl = URI.create("localhost:8080/api");
        assertThat(SignatureServiceRoot.from(STAGING.withServiceUrl(localhostUrl)), where(SignatureServiceRoot::rootUrl, is(localhostUrl)));
    }

    @Test
    void relativeUrlsCanNotBeServiceRoots() {
        assertAll(Stream.of("localhost", "some/path", "/absolute/path/but/still/relative/url")
                .map(URI::create)
                .map(relativeUrl -> () -> assertThrows(IllegalArgumentException.class, () -> new SignatureServiceRoot(relativeUrl))));
    }

    @Test
    void constructUrls() {
        URI url = SignatureServiceRoot.from(STAGING).constructUrl(uri -> uri.appendPathSegments("jobs", "42", "pades"));
        assertThat(url, where(URI::toString, is(STAGING.signatureServiceRootUrl() + "/jobs/42/pades")));
    }




}
