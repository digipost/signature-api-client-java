package no.digipost.signature.client;

import java.time.Duration;

public interface TimeoutsConfigurer {

    TimeoutsConfigurer socketTimeout(Duration duration);

    TimeoutsConfigurer connectTimeout(Duration duration);

    TimeoutsConfigurer connectionRequestTimeout(Duration duration);

    TimeoutsConfigurer responseArrivalTimeout(Duration duration);


    /**
     * Allows setting all timeouts offered by {@link TimeoutsConfigurer} to
     * a common duration. This is usually not applicable for any real
     * integration, as one would usually want to tweak discrete timeout
     * values.
     * <p>
     * The primary intention for this is to allow setting
     * infinite timeouts ({@link Duration#ZERO}) for debugging, or other
     * troubleshooting scenarios.
     * <strong>Never set an infinite timeout for your production environment.</strong>
     */
    default TimeoutsConfigurer allTimeouts(Duration duration) {
        return socketTimeout(duration)
                .connectTimeout(duration)
                .connectionRequestTimeout(duration)
                .responseArrivalTimeout(duration);
    }

}
