package no.digipost.signature.client.core.internal;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.text.Normalizer.Form.NFD;
import static java.util.Arrays.binarySearch;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.rangeClosed;

public final class FileName {

    private static final Map<Pattern, String> knownReplacements; static {
        knownReplacements = new LinkedHashMap<>();
        knownReplacements.put(Pattern.compile("[\\s:;&%\\$\\*\\?\\+=@,\\(\\)\\[\\]\\{\\}#!\"“”\\^`´<>]+"), "_"); //various punctuation
        knownReplacements.put(Pattern.compile("[æÆ]"), "ae");
        knownReplacements.put(Pattern.compile("[øØ]"), "oe");
        knownReplacements.put(Pattern.compile("[åÅ]"), "aa");
    }

    private static final char[] allowedChars = Stream.of(
            rangeClosed('0', '9'),
            rangeClosed('a', 'z'),
            IntStream.of('-', '_', '.'))
        .flatMapToInt(identity())
        .sorted()
        .mapToObj(c -> String.valueOf((char) c))
        .collect(joining())
        .toCharArray();


    public static String reduceToFileNameSafeChars(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException(text);
        }
        String knownReplacements = text;
        for (Entry<Pattern, String> replacement : FileName.knownReplacements.entrySet()) {
            knownReplacements = replacement.getKey().matcher(knownReplacements).replaceAll(replacement.getValue());
        }
        String accentsRemoved = removeAccents(knownReplacements);
        return reduceToAlphabet(accentsRemoved.toLowerCase(), allowedChars);
    }


    /**
     * Reduce a text to only contain characters of a given alphabet
     * using the following algorithm:
     * <ul>
     *   <li>characters already part of the alphabet are kept as-is</li>
     *   <li>characters not part of the alphabet is replaced with a
     *       character from the alphabet in a non-defined, but repeatable manner.</li>
     * </ul>
     *
     * @param text the source text
     * @param alphabet the allowed characters in the resulting String
     * @return the resulting string
     */
    private static String reduceToAlphabet(String text, char[] alphabet) {
        StringBuilder reducedToAllowedChars = new StringBuilder(text.length());
        for (int c : text.toCharArray()) {
            if (binarySearch(allowedChars, (char) c) >= 0) {
                reducedToAllowedChars.append((char) c);
            } else {
                reducedToAllowedChars.append(allowedChars[c % allowedChars.length]);
            }
        }
        return reducedToAllowedChars.toString();
    }


    private static final Pattern UNICODE_ACCENT = Pattern.compile("\\p{M}");

    /**
     * Replaces accented characters (è, ü, etc) with their base glyphs (e, u, etc).
     * @see https://stackoverflow.com/questions/3322152/is-there-a-way-to-get-rid-of-accents-and-convert-a-whole-string-to-regular-lette
     */
    private static String removeAccents(String text) {
        return UNICODE_ACCENT.matcher(Normalizer.normalize(text, NFD)).replaceAll("");
    }

    private FileName() {
    }
}
