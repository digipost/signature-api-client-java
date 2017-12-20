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
package no.digipost.signature.client.core;

import java.util.Objects;

public class PollingQueue {

    public final static PollingQueue DEFAULT_QUEUE = new PollingQueue(null);

    public final String value;

    private PollingQueue(String value) {
        this.value = value;
    }

    public static PollingQueue of(String value) {
        return new PollingQueue(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PollingQueue) {
            PollingQueue that = (PollingQueue) obj;
            return Objects.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

}
