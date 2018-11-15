package no.digipost.signature.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

final class ClientMetadata {

    static final String VERSION;


    static {
        String version = "unknown version";
        try (InputStream versionFile = ClientMetadata.class.getResourceAsStream("version"); Scanner scanner = new Scanner(versionFile, "UTF-8")) {
            version = scanner.next();
        } catch (IOException e) {
            Logger log = LoggerFactory.getLogger(ClientMetadata.class);
            log.warn("Unable to resolve library version from classpath resource 'version', because {}: '{}'", e.getClass().getSimpleName(), e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            } else {
                log.info("Enable debug-logging for logger '{}' to see full stacktrace for above warning" + log.getName());
            }
        } finally {
            VERSION = version;
        }
    }

    private ClientMetadata() {}
}
