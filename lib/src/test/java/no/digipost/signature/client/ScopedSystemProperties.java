package no.digipost.signature.client;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

public final class ScopedSystemProperties implements AutoCloseable {

    private final Map<String, Optional<String>> previousProperties;

    public ScopedSystemProperties(Map<String, String> properties) {
        this.previousProperties = properties.entrySet().stream().collect(toMap(Entry::getKey, ScopedSystemProperties::setSystemProperty));
    }

    public ScopedSystemProperties(String ... alternatingKeysAndValues) {
        this(assembleMapFromAlternatingKeysAndValues(alternatingKeysAndValues));
    }

    private static Map<String, String> assembleMapFromAlternatingKeysAndValues(String ... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Must be an even number of values in order to assemble into keys and values, " +
                    "but got " + keysAndValues.length + " values: " + Arrays.toString(keysAndValues));
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i = i + 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return unmodifiableMap(map);
    }


    @Override
    public void close() {
        previousProperties.forEach(ScopedSystemProperties::setSystemProperty);
    }

    private static Optional<String> setSystemProperty(Map.Entry<String, String> property) {
        return setSystemProperty(property.getKey(), Optional.ofNullable(property.getValue()));
    }

    private static Optional<String> setSystemProperty(String key, Optional<String> value) {
        return value
                .map(v -> Optional.ofNullable(System.setProperty(key, v)))
                .orElseGet(() -> Optional.ofNullable(System.clearProperty(key)));
    }
}
