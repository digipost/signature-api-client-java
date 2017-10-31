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
package no.digipost.signature.client.core.exceptions;

import java.time.Instant;
import java.time.ZonedDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class TooEagerPollingException extends RuntimeException {

    private final Instant nextPermittedPollTime;

    public TooEagerPollingException(String nextPermittedPollTime) {
        this.nextPermittedPollTime = ZonedDateTime.parse(nextPermittedPollTime, ISO_DATE_TIME).toInstant();
    }

    public Instant getNextPermittedPollTime() {
        return nextPermittedPollTime;
    }
}
