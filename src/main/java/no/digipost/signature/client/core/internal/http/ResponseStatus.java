package no.digipost.signature.client.core.internal.http;

import no.digipost.signature.client.core.exceptions.UnexpectedResponseException;
import org.apache.hc.core5.http.HttpResponse;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ResponseStatus {

    public static ResponseStatus fromHttpResponse(HttpResponse response) {
        return response != null ? fromHttpStatusCode(response.getCode()) : null;
    }

    public static ResponseStatus fromHttpStatusCode(int statusCode) {
        return new ResponseStatus(new StatusCode(statusCode), s -> true);
    }

    private final StatusCode statusCode;
    private final Predicate<StatusCode> statusExpectation;

    private ResponseStatus(StatusCode statusCode, Predicate<StatusCode> expectation) {
        this.statusCode = statusCode;
        this.statusExpectation = expectation;
    }

    public ResponseStatus expect(StatusCodeFamily expectedStatusFamily) {
        return expect(s -> s.is(expectedStatusFamily));
    }

    public ResponseStatus expectOneOf(StatusCodeFamily ... expectedStatusFamilies) {
        return expectOneOf(Stream.of(expectedStatusFamilies), (family, statusCode) -> statusCode.is(family));
    }

    private <T> ResponseStatus expectOneOf(Stream<T> expecteds, BiPredicate<T, StatusCode> expectedEvaluator) {
        Predicate<StatusCode> oneOfExpectedsAndAlsoExistingExpectation =
                expecteds
                .map(expected -> (Predicate<StatusCode>) status -> expectedEvaluator.test(expected, status))
                .reduce(Predicate::or)
                .map(statusExpectation::and)
                .orElse(statusExpectation);

        return expect(oneOfExpectedsAndAlsoExistingExpectation);
    }

    public ResponseStatus expect(Predicate<? super StatusCode> expectation) {
        return new ResponseStatus(statusCode, this.statusExpectation.and(expectation));
    }

    public <X extends Exception> ResponseStatus throwIf(int status, Function<StatusCode, X> exceptionSupplier) throws X {
        return throwIf(s -> s.equals(StatusCode.from(status)), exceptionSupplier);
    }

    public <X extends Exception> ResponseStatus throwIf(Predicate<? super StatusCode> illegalStatus, Function<StatusCode, X> exceptionSupplier) throws X {
        if (illegalStatus.test(statusCode)) {
            throw exceptionSupplier.apply(statusCode);
        } else {
            return this;
        }
    }


    public StatusCode get() {
        return orThrow(s -> new UnexpectedResponseException(statusCode));
    }


    public <X extends Exception> StatusCode orThrow(Function<StatusCode, X> exceptionSupplier) throws X {
        return throwIf(statusExpectation.negate(), exceptionSupplier).statusCode;
    }

    @Override
    public String toString() {
        return statusCode.toString() + (statusExpectation.test(statusCode) ? "" : " (unexpected)");
    }

}
