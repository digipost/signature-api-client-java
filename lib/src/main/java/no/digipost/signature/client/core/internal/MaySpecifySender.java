package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.SenderNotSpecifiedException;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

public interface MaySpecifySender {

    static final MaySpecifySender NO_SPECIFIED_SENDER = new NoSenderSpecified();

    static MaySpecifySender specifiedAs(Sender sender) {
        return new SpecifiedSender(sender);
    }

    static MaySpecifySender ofNullable(Sender sender) {
        return sender != null ? specifiedAs(sender) : NO_SPECIFIED_SENDER;
    }


    Optional<Sender> getSender();

    default Sender resolveSenderWithFallbackTo(MaySpecifySender firstFallback, MaySpecifySender ... moreFallbacks) {
        return resolveSenderWithFallbackTo(
                moreFallbacks.length == 0 ? Stream.of(firstFallback) : concat(Stream.of(firstFallback), Stream.of(moreFallbacks)));
    }

    default Sender resolveSenderWithFallbackTo(Stream<MaySpecifySender> fallbacks) {
        return getSender().orElseGet(() -> fallbacks.map(MaySpecifySender::getSender)
                .filter(s -> s.isPresent()).findFirst().map(Optional::get)
                .orElseThrow(SenderNotSpecifiedException::new));
    }
}


final class SpecifiedSender implements MaySpecifySender {
    private final Optional<Sender> sender; //always present

    public SpecifiedSender(Sender sender) {
        this.sender = Optional.of(sender);
    }

    @Override
    public Optional<Sender> getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return "specified as " + sender.get();
    }
}


final class NoSenderSpecified implements MaySpecifySender {
    private static final Optional<Sender> NO_SENDER = Optional.empty();

    @Override
    public Optional<Sender> getSender() {
        return NO_SENDER;
    }

    @Override
    public String toString() {
        return "no Sender specified";
    }
}
