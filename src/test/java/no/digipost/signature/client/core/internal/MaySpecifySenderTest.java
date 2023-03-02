package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.SenderNotSpecifiedException;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static no.digipost.signature.client.core.internal.MaySpecifySender.NO_SPECIFIED_SENDER;
import static no.digipost.signature.client.core.internal.MaySpecifySender.specifiedAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaySpecifySenderTest {

    @Test
    void throwsIfNoSenderIsAvailable() {
        assertThrows(SenderNotSpecifiedException.class, () -> NO_SPECIFIED_SENDER.resolveSenderWithFallbackTo(Stream.empty()));
        assertThrows(SenderNotSpecifiedException.class, () -> NO_SPECIFIED_SENDER.resolveSenderWithFallbackTo(NO_SPECIFIED_SENDER));
        assertThrows(SenderNotSpecifiedException.class, () -> NO_SPECIFIED_SENDER.resolveSenderWithFallbackTo(NO_SPECIFIED_SENDER, NO_SPECIFIED_SENDER));
    }

    @Test
    void prioritizesFirstSender() {
        Sender sender = new Sender("123456789");
        assertThat(specifiedAs(sender).resolveSenderWithFallbackTo(NO_SPECIFIED_SENDER), sameInstance(sender));
        assertThat(NO_SPECIFIED_SENDER.resolveSenderWithFallbackTo(specifiedAs(sender)), sameInstance(sender));
        assertThat(specifiedAs(sender).resolveSenderWithFallbackTo(specifiedAs(new Sender("987654321"))), sameInstance(sender));
    }

}
