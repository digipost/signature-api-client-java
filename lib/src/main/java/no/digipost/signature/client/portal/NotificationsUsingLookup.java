package no.digipost.signature.client.portal;

public enum NotificationsUsingLookup {

    EMAIL_ONLY(true, false),   //Email notifications can't be turned off
    EMAIL_AND_SMS(true, true);

    public final boolean shouldSendEmail;
    public final boolean shouldSendSms;

    NotificationsUsingLookup(boolean sendEmail, boolean sendSms) {
        this.shouldSendEmail = sendEmail;
        this.shouldSendSms = sendSms;
    }

}
