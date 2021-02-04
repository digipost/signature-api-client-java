package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.SenderNotSpecifiedException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActualSenderTest {

    @Test
    void throwsIfNoSenderIsAvailable() {
        assertThrows(SenderNotSpecifiedException.class, () -> ActualSender.getActualSender(Optional.empty(), Optional.empty()));
    }

    @Test
    void prioritizesSenderOnJob() {
        Sender senderOnJob = new Sender("123456789");
        Sender actualSender = ActualSender.getActualSender(Optional.of(senderOnJob), Optional.empty());
        assertThat(actualSender, sameInstance(senderOnJob));
    }

    @Test
    void fallsBackToGlobalSender() {
        Sender globalSender = new Sender("123456789");
        Sender actualSender = ActualSender.getActualSender(Optional.empty(), Optional.of(globalSender));
        assertThat(actualSender, sameInstance(globalSender));
    }


}
