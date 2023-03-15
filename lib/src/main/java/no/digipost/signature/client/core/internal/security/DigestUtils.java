package no.digipost.signature.client.core.internal.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public final class DigestUtils {

    public enum Algorithm {
        SHA256("SHA-256"),
        SHA1("SHA-1");

        final String algorithmName;
        final Provider provider;

        Algorithm(String algorithmName) {
            this.algorithmName = algorithmName;
            String providerSpec = "MessageDigest." + algorithmName;
            this.provider = Stream.of(Security.getProviders(providerSpec)).findFirst()
                    .orElseThrow(() -> new NoSuchElementException("java.security Provider for " + providerSpec));
        }

        @Override
        public String toString() {
            return algorithmName + " provided by " + provider;
        }
    }

    public static byte[] digest(Algorithm algorithm, byte[] bytes) {
        return getMessageDigest(algorithm).digest(bytes);
    }

    private static MessageDigest getMessageDigest(Algorithm algorithm) {
        try {
            return MessageDigest.getInstance(algorithm.algorithmName, algorithm.provider);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(algorithm + ": " + e.getClass().getSimpleName() + " '" + e.getMessage() + "'", e);
        }
    }


}
