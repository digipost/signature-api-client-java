package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.Sender;

import java.util.Optional;

import static no.digipost.signature.client.core.exceptions.SenderNotSpecifiedException.SENDER_NOT_SPECIFIED;

public final class ActualSender {
    public static Sender getActualSender(Optional<Sender> messageSpecificSender, Optional<Sender> globalSender) {
        return messageSpecificSender.orElseGet(() -> globalSender.orElseThrow(SENDER_NOT_SPECIFIED));
    }

    private ActualSender() {
    }
}
