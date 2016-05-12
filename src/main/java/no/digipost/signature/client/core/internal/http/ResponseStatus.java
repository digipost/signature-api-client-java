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
package no.digipost.signature.client.core.internal.http;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import java.util.Objects;

public class ResponseStatus {

    public static StatusType resolve(int code) {
        StatusType status = Status.fromStatusCode(code);
        if (status == null) {
            status = Custom.fromStatusCode(code);
        }
        if (status == null) {
            status = unknown(code);
        }
        return status;
    }



    /**
     * Status codes not part of the JAX-RS {@link Status} enum.
     */
    public enum Custom implements StatusType {

        /**
         * 422 Unprocesable Entity, see
         * <a href="https://tools.ietf.org/html/rfc4918#section-11.2">https://tools.ietf.org/html/rfc4918#section-11.2</a>
         */
        UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),

        /**
         * 429 Too Many Requests, see
         * <a href="https://tools.ietf.org/html/rfc6585#page-3">https://tools.ietf.org/html/rfc6585#page-3</a>
         */
        TOO_MANY_REQUESTS(429, "Too Many Requests");

        /**
         * Convert a numerical status code into the corresponding CustomStatus.
         *
         * @param code the numerical status code.
         * @return the matching Status or null is no matching Status is defined.
         */
        public static ResponseStatus.Custom fromStatusCode(int code) {
            for (Custom s : Custom.values()) {
                if (s.code == code) {
                    return s;
                }
            }
            return null;
        }



        private int code;
        private String reason;
        private Family family;

        Custom(int code, String reasonPhrase) {
            this.code = code;
            this.reason = reasonPhrase;
            this.family = Family.familyOf(code);
        }

        @Override
        public int getStatusCode() {
            return code;
        }

        @Override
        public Family getFamily() {
            return family;
        }

        @Override
        public String getReasonPhrase() {
            return reason;
        }

        @Override
        public String toString() {
            return code + " " + reason;
        }
    }



    public static StatusType unknown(int code) {
        return new Unknown(code);
    }

    public static final class Unknown implements StatusType {

        final int code;
        final Family family;
        final String reason;

        private Unknown(int code) {
            this.code = code;
            this.family = Family.familyOf(code);
            this.reason = "(" + family + ", unrecognized status code)";
        }


        @Override
        public int getStatusCode() {
            return code;
        }

        @Override
        public String getReasonPhrase() {
            return reason;
        }

        @Override
        public Family getFamily() {
            return family;
        }

        @Override
        public String toString() {
            return code + " " + reason;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ResponseStatus.Unknown) {
                ResponseStatus.Unknown that = (ResponseStatus.Unknown) obj;
                return Objects.equals(this.code, that.code);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(code);
        }
    }


}