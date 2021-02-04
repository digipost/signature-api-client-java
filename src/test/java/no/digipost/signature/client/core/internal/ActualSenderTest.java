package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.Sender;
import no.digipost.signature.client.core.exceptions.SenderNotSpecifiedException;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ActualSenderTest {

    @Test
    public void throwsIfNoSenderIsAvailable() {
        try {
            ActualSender.getActualSender(Optional.empty(), Optional.empty());
        } catch (SenderNotSpecifiedException e) {
            return;
        }
        fail("should have thrown exception");
    }

    @Test
    public void prioritizesSenderOnJob() {
        Sender senderOnJob = new Sender("123456789");
        Sender actualSender = ActualSender.getActualSender(Optional.of(senderOnJob), Optional.empty());
        assertThat(actualSender, sameInstance(senderOnJob));
    }

    @Test
    public void fallsBackToGlobalSender() {
        Sender globalSender = new Sender("123456789");
        Sender actualSender = ActualSender.getActualSender(Optional.empty(), Optional.of(globalSender));
        assertThat(actualSender, sameInstance(globalSender));
    }


}
