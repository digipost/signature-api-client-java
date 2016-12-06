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
package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.AuthenticationLevel;
import no.digipost.signature.client.core.Sender;

import java.util.UUID;

/**
 * Provides operations for customizing jobs using builder-type methods for
 * properties which are common for both Direct and Portal jobs.
 * You would not under normal circumstances refer to this type.
 */
public interface JobCustomizations<B extends JobCustomizations<B>> {


    /**
     * Set the sender for this specific signature job.
     * <p>
     * You may use {@link no.digipost.signature.client.ClientConfiguration.Builder#globalSender(Sender)}
     * to specify a global sender used for all signature jobs.
     */
    public B withSender(Sender sender);


    /**
     * Specify the minimum level of authentication of the signer(s) of this job. This
     * includes the required authentication both in order to <em>view</em> the document, as well
     * as it will limit which <em>authentication mechanisms offered at the time of signing</em>
     * the document.
     *
     * @param level the required minimum {@link AuthenticationLevel}.
     */
    B requireAuthentication(AuthenticationLevel minimumLevel);


    /**
     * Set an {@link UUID} as custom reference that is attached to the job.
     *
     * @param uuid the {@link UUID} to use as reference.
     */
    B withReference(UUID uuid);


    /**
     * Set a custom reference that is attached to the job.
     *
     * @param reference the reference
     */
    B withReference(String reference);

}
