package no.digipost.signature.client.portal;

public class Notifications {

    private String emailAddress = null;
    private String mobileNumber = null;

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public boolean shouldSendEmail() {
        return emailAddress != null;
    }

    public boolean shouldSendSms() {
        return mobileNumber != null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Notifications target;
        private boolean built = false;

        private Builder() {
            target = new Notifications();
        }

        public Builder withEmailTo(String emailAddress) {
            target.emailAddress = emailAddress;
            return this;
        }

        public Builder withSmsTo(String mobileNumber) {
            target.mobileNumber = mobileNumber;
            return this;
        }

        public Notifications build() {
            if (built) throw new IllegalStateException("Can't build twice");
            if (target.emailAddress == null && target.mobileNumber == null) {
                throw new IllegalStateException("At least one way of notifying the signer must be specified");
            }
            built = true;
            return target;
        }
    }

    @Override
    public String toString() {
        if (emailAddress != null && mobileNumber != null) {
            return "Notifications to " + emailAddress + " and " + mobileNumber;
        } else if (emailAddress != null) {
            return "Notification to " + emailAddress;
        } else if (mobileNumber != null) {
            return "Notification to " + mobileNumber;
        } else {
            return "No notifications";
        }
    }
}
