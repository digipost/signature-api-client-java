package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.core.exceptions.UnexpectedResponseException;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ResponseStatus {

    public static ResponseStatus resolve(int code) {
        StatusType status = Status.fromStatusCode(code);
        if (status == null) {
            status = Custom.fromStatusCode(code);
        }
        if (status == null) {
            status = unknown(code);
        }
        return new ResponseStatus(status, s -> true);
    }

    private final StatusType status;
    private final Predicate<StatusType> statusExpectation;

    private ResponseStatus(StatusType status, Predicate<StatusType> expectation) {
        this.status = status;
        this.statusExpectation = expectation;
    }

    public ResponseStatus expect(Status.Family expectedStatusFamily) {
        return expect(s -> s.getFamily() == expectedStatusFamily);
    }

    public ResponseStatus expectOneOf(Status.Family ... expectedStatusFamilies) {
        return expectOneOf(Stream.of(expectedStatusFamilies), (family, status) -> status.getFamily() == family);
    }

    private <T> ResponseStatus expectOneOf(Stream<T> expecteds, BiPredicate<T, StatusType> expectedEvaluator) {
        Predicate<StatusType> oneOfExpectedsAndAlsoExistingExpectation =
                expecteds
                .map(expected -> (Predicate<StatusType>) status -> expectedEvaluator.test(expected, status))
                .reduce(Predicate::or)
                .map(statusExpectation::and)
                .orElse(statusExpectation);

        return expect(oneOfExpectedsAndAlsoExistingExpectation);
    }

    public ResponseStatus expect(Predicate<? super StatusType> expectation) {
        return new ResponseStatus(status, this.statusExpectation.and(expectation));
    }

    public <X extends Exception> ResponseStatus throwIf(Status status, Function<StatusType, X> exceptionSupplier) throws X {
        return throwIf(s -> s.equals(status), exceptionSupplier);
    }

    public <X extends Exception> ResponseStatus throwIf(Predicate<? super StatusType> illegalStatus, Function<StatusType, X> exceptionSupplier) throws X {
        if (illegalStatus.test(status)) {
            throw exceptionSupplier.apply(status);
        } else {
            return this;
        }
    }


    public StatusType get() {
        return orThrow(s -> new UnexpectedResponseException(status));
    }


    public <X extends Exception> StatusType orThrow(Function<StatusType, X> exceptionSuppplier) throws X {
        return throwIf(statusExpectation.negate(), exceptionSuppplier).status;
    }

    @Override
    public String toString() {
        return status.toString() + (statusExpectation.test(status) ? "" : " (unexpected)");
    }




    /**
     * Status codes not part of the JAX-RS {@link Status} enum.
     */
    public enum Custom implements StatusType {

        /**
         * 422 Unprocessable Entity, see
         * <a href="https://tools.ietf.org/html/rfc4918#section-11.2">https://tools.ietf.org/html/rfc4918#section-11.2</a>
         */
        UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),

        ;

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



    static StatusType unknown(int code) {
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
