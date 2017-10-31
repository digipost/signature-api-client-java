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

import java.util.function.Supplier;

public class SenderNotSpecifiedException extends SignatureException {

    public static final Supplier<SignatureException> SENDER_NOT_SPECIFIED = SenderNotSpecifiedException::new;

    private SenderNotSpecifiedException() {
        super("Sender is not specified. Please call ClientConfiguration#sender to set it globally, " +
                "or DirectJob.Builder#withSender or PortalJob.Builder#withSender if you need to specify sender " +
                "on a per job basis (typically when acting as a broker on behalf of multiple senders).");
    }
}
