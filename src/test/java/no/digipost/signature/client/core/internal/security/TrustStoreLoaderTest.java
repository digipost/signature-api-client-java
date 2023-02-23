package no.digipost.signature.client.core.internal.security;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.list;
import static java.util.stream.Collectors.toList;
import static no.digipost.signature.client.ServiceEnvironment.DIFITEST;
import static no.digipost.signature.client.ServiceEnvironment.PRODUCTION;
import static no.digipost.signature.client.core.internal.security.TrustStoreLoader.generateAlias;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.strings;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

class TrustStoreLoaderTest {

    @Test
    void loads_productions_certificates() throws KeyStoreException {
        KeyStore trustStore = TrustStoreLoader.build(PRODUCTION);

        assertThat(trustStore, containsExactlyTheAliases(
                "prod:bpclass3rootca.cer",
                "prod:bpclass3ca3.cer",
                "prod:bpcl3rootcag2st.cer",
                "prod:bpcl3cag2stbs.cer",
                "prod:bpcl3rootcag2ht.cer",
                "prod:bpcl3cag2htbs.cer",
                "prod:commfides_root_ca.cer",
                "prod:commfides_ca.cer"));
    }

    @Test
    void loads_test_certificates() throws KeyStoreException {
        KeyStore trustStore = TrustStoreLoader.build(DIFITEST);

        assertThat(trustStore, containsExactlyTheAliases(
                "test:buypass_class_3_test4_root_ca.cer",
                "test:buypass_class_3_test4_ca_3.cer",
                "test:bpcl3rootcag2st.cer",
                "test:bpcl3cag2stbs.cer",
                "test:bpcl3rootcag2ht.cer",
                "test:bpcl3cag2htbs.cer",
                "test:commfides_test_root_ca.cer",
                "test:commfides_test_ca.cer",
                "test:digipost_test_root_ca.cert.pem"));
    }

    @Test
    void loads_certificates_from_file_location() throws KeyStoreException {
        KeyStore trustStore = TrustStoreLoader.build(DIFITEST.withCertificates("./src/test/files/certificateTest"));

        assertThat(trustStore, containsExactlyTheAliases("certificatetest:commfides_test_ca.cer"));
    }

    @Nested
    class GenerateAlias {
        @Test
        void generateAliasFromUnixPath() {
            assertThat(generateAlias(Paths.get("/blah/blah/funny/env/MyCert.cer")), is("env:MyCert.cer"));
        }

        @Test
        void generateAliasFromWindowsPath() {
            assertThat(generateAlias(Paths.get("C:/blah/blah/funny/env/MyCert.cer")), is("env:MyCert.cer"));
        }

        @Test
        void generateAliasFromUnixFileInJarUrlString() {
            assertThat(generateAlias("/blah/fun/prod/WEB-INF/lib/mylib.jar!/certificates/env/MyCert.cer"), is("env:MyCert.cer"));
        }

        @Test
        void generateAliasFromWindowsFileInJarUrlString() {
            assertThat(generateAlias("file:/C:/blah/fun/prod/WEB-INF/lib/mylib.jar!/certificates/env/MyCert.cer"), is("env:MyCert.cer"));
        }

        @Test
        void generateUniqueDefaultAliasesForNullsAndEmptyStrings() {
            List<String> defaultAliases = Stream.<String>of(null, "", " ", "   \n ").map(s -> TrustStoreLoader.generateAlias(s)).collect(toList());
            int aliasCount = defaultAliases.size();
            assertAll("default aliases",
                    () -> assertThat(defaultAliases, everyItem(matchesRegex("certificate-alias-\\d+"))),
                    () -> assertAll(IntStream.range(0, aliasCount).mapToObj(defaultAliases::get).map(alias -> () -> {
                        List<String> otherAliases = defaultAliases.stream().filter(a -> !alias.equals(a)).collect(toList());
                        assertThat("other alises than '" + alias + "'", otherAliases, hasSize(aliasCount - 1));
                    })));
        }

        @Test
        void alwaysGeneratesAnAlias() {
            qt()
                .forAll(strings().allPossible().ofLengthBetween(0, 100))
                .checkAssert(s -> assertThat(s, where(TrustStoreLoader::generateAlias, notNullValue())));
        }


    }


    private static Matcher<KeyStore> containsExactlyTheAliases(String ... certAliases) {
        return new TypeSafeDiagnosingMatcher<KeyStore>() {

            @Override
            public void describeTo(Description description) {
                description
                    .appendText("key store containing " + certAliases.length + " certificates with aliases: ")
                    .appendValueList("", ", ", "", certAliases);
            }

            @Override
            protected boolean matchesSafely(KeyStore keyStore, Description mismatchDescription) {
                try {
                    List<String> actualAliases = list(keyStore.aliases());
                    Matcher<Iterable<? extends String>> expectedAliases = containsInAnyOrder(certAliases);
                    if (!expectedAliases.matches(actualAliases)) {
                        expectedAliases.describeMismatch(actualAliases, mismatchDescription);
                        return false;
                    } else if (keyStore.size() != certAliases.length) {
                        mismatchDescription.appendText("contained " + keyStore.size() + " certificates");
                    }
                    return true;
                } catch (KeyStoreException e) {
                    mismatchDescription
                        .appendText("threw exception retrieving the aliases from the key store: ")
                        .appendValue(e.getClass().getSimpleName());
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

        };
    }

}
