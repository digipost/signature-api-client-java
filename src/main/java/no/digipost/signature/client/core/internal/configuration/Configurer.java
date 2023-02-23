package no.digipost.signature.client.core.internal.configuration;

public interface Configurer<C> {

    static <C> Configurer<C> notConfigured() {
        @SuppressWarnings("unchecked")
        Configurer<C> castedInstance = (Configurer<C>) NotConfigured.INSTANCE;
        return castedInstance;
    }

    void applyTo(C configurable);

    default Configurer<C> andThen(Configurer<C> next) {
        return configurable -> {
            this.applyTo(configurable);
            next.applyTo(configurable);
        };
    }

}

final class NotConfigured<C> implements Configurer<C> {

    static final NotConfigured<?> INSTANCE = new NotConfigured<>();

    @Override
    public void applyTo(C configurable) {
    }

}
