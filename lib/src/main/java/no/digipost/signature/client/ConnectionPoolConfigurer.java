package no.digipost.signature.client;

/**
 * Allows configuring aspects of the connection pool
 * used by the internal HTTP client.
 */
public interface ConnectionPoolConfigurer {

    /**
     * The default amount of connections in the connection pool is
     * {@value #DEFAULT_TOTAL_CONNECTIONS_IN_POOL}.
     * This may be overridden using {@link #maxTotalConnectionsInPool(int)}
     */
    int DEFAULT_TOTAL_CONNECTIONS_IN_POOL = 10;


    /**
     * Set the amount of connections in the connection pool, if the default
     * of {@value #DEFAULT_TOTAL_CONNECTIONS_IN_POOL} is not applicable for
     * your integration.
     *
     * @param count the amount of connections in the connection pool
     */
    ConnectionPoolConfigurer maxTotalConnectionsInPool(int count);

}
