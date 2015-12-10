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

/**
 * An entity received from the Signature API which must be confirmed
 * as received by the client. The confirmation may result in resource(s)
 * being made unavailable to the client. Typically, if the confirmation is
 * for received status of a complete (signed or cancelled) job, the server
 * is free to handle the job as it see fit, e.g make the job unavailable to
 * the client through the API.
 * <p>
 *   <strong>Confirming is a required part of the communication protocol with
 *   the Signature API.</strong>
 * <p>
 * Please refer to the documentation of each confirmation case for any specific
 * consequences of confirming a received entity.
 */
public interface Confirmable {

    ConfirmationReference getConfirmationReference();

}
