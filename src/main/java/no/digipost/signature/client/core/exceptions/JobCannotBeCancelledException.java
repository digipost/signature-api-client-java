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

import javax.ws.rs.core.Response.StatusType;

public class JobCannotBeCancelledException extends SignatureException {

    public JobCannotBeCancelledException(StatusType status, String errorCode, String errorMessageFromServer) {
        super("The service refused to process the cancellation. This happens when the job has been completed " +
              "(i.e. all signers have signed or rejected, the job has expired, etc.) since receiving last update. " +
              "Please ask the service for status changes to get the latest changes. The server response was " +
              status.getStatusCode() + " " + status.getReasonPhrase() + " '" + errorMessageFromServer + "'");
    }

}
