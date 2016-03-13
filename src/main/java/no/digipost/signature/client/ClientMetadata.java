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
package no.digipost.signature.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public final class ClientMetadata {

    public static final String VERSION;


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
