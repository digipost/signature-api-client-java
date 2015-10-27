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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TooEagerPollingException extends RuntimeException {

    private final Date nextPermittedPollTime;

    public TooEagerPollingException(String nextPermittedPollTime) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            this.nextPermittedPollTime = formatter.parse(nextPermittedPollTime);
        } catch (ParseException e) {
            throw new RuntimeException();
        }
    }

    public Date getNextPermittedPollTime() {
        return nextPermittedPollTime;
    }
}
