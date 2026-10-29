package no.digipost.signature.client.core.internal;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class NamedValue<T> {
    private final String name;
    private final T value;
    private final String description;

    protected NamedValue(String name, T value) {
        this(name, value, Objects.toString(value));
    }

    protected NamedValue(String name, T value, String valueDescription) {
        this.name = requireNonNull(name, "name");
        this.value = requireNonNull(value, () -> "value for " + name);
        this.description = name + ": " + valueDescription;
    }

    public final String name() {
        return name;
    }

    public final T value() {
        return value;
    }

    @Override
    public final String toString() {
        return description;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj != null && this.getClass() == obj.getClass()) {
            NamedValue<?> that = (NamedValue<?>) obj;
            return Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value);
        } else {
            return false;
        }
    }

    @Override
    public final int hashCode() {
        return 31 * Objects.hashCode(name) + Objects.hashCode(value);
    }

}
