package no.digipost.signature.client.core.internal.configuration;

import java.util.function.Consumer;

public interface Configurer<C> {

    static <C> Configurer<C> notConfigured() {
        @SuppressWarnings("unchecked")
        Configurer<C> castedInstance = (Configurer<C>) NotConfigured.INSTANCE;
        return castedInstance;
    }

    static <C> Configurer<C> of(Consumer<C> consumer) {
        return consumer::accept;
    }

    void applyTo(C configurable);

    default Configurer<C> andThen(Configurer<? super C> next) {
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
