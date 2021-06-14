package no.digipost.signature.client.core.internal;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static no.digipost.signature.client.core.internal.FileName.reduceToFileNameSafeChars;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.strings;

class FileNameTest {

    @Test
    void convertsToLowerCase() {
        assertThat(reduceToFileNameSafeChars("Title"), is("title"));
    }

    @Test
    void replacesNorwegianCharacters() {
        assertThat(reduceToFileNameSafeChars("ÆØÅ"), is("aeoeaa"));
        assertThat(reduceToFileNameSafeChars("æøå"), is("aeoeaa"));
    }

    @Test
    void successiveSpacesIsReplacedWithAnUnderscore() {
        assertThat(reduceToFileNameSafeChars("Hei   på \t  Deg"), is("hei_paa_deg"));
    }

    @Test
    void successivePunctuationIsReplacedWithAnUnderscore() {
        assertThat(reduceToFileNameSafeChars("*@,:;&%$^?+=()[]{}#\"“”!`´<>"), is("_"));
    }

    @Test
    void emptyNameIsAnError() {
        assertThrows(IllegalArgumentException.class, () -> reduceToFileNameSafeChars(""));
        assertThrows(IllegalArgumentException.class, () -> reduceToFileNameSafeChars(null));
    }

    @Test
    void accentsAndOtherDiacriticalMarksAreRemoved() {
        assertThat(reduceToFileNameSafeChars("àáëêÈÉüÜïñÑ"), is("aaeeeeuuinn"));
    }

    @Test
    void periodAndDashAreKeptAsIs() {
        assertThat(reduceToFileNameSafeChars("Morse ...---..."), is("morse_...---..."));
    }

    @Test
    void urlEncodingWillAlwaysProduceSameString() {
        qt()
            .forAll(strings().allPossible().ofLengthBetween(1, 100))
            .as(FileName::reduceToFileNameSafeChars)
            .checkAssert(safeFileName -> assertThat(urlEncode(safeFileName), equalTo(safeFileName)));
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException utf8NotSupported) {
            throw new RuntimeException(utf8NotSupported.getMessage(), utf8NotSupported);
        }
    }

}
