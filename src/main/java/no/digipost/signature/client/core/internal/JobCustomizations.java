package no.digipost.signature.client.core.internal;

import no.digipost.signature.client.core.AuthenticationLevel;
import no.digipost.signature.client.core.IdentifierInSignedDocuments;
import no.digipost.signature.client.core.Sender;

import java.util.UUID;

/**
 * Provides operations for customizing jobs using builder-type methods for
 * properties which are common for both Direct and Portal jobs.
 * You would not under normal circumstances refer to this type.
 */
public interface JobCustomizations<B extends JobCustomizations<B>> {


    /**
     * Set the sender for this specific signature job.
     * <p>
     * You may use {@link no.digipost.signature.client.ClientConfiguration.Builder#globalSender(Sender)}
     * to specify a global sender used for all signature jobs.
     */
    B withSender(Sender sender);


    /**
     * Specify the minimum level of authentication of the signer(s) of this job. This
     * includes the required authentication both in order to <em>view</em> the document, as well
     * as it will limit which <em>authentication mechanisms offered at the time of signing</em>
     * the document.
     *
     * @param minimumLevel the required minimum {@link AuthenticationLevel}.
     */
    B requireAuthentication(AuthenticationLevel minimumLevel);


    /**
     * Set an {@link UUID} as custom reference that is attached to the job.
     *
     * @param uuid the {@link UUID} to use as reference.
     */
    B withReference(UUID uuid);


    /**
     * Set a custom reference that is attached to the job.
     *
     * @param reference the reference
     */
    B withReference(String reference);

    /**
     *  Specify how the signer(s) of this job should be identified in the signed documents (XAdES and PAdES);
     *  by {@link IdentifierInSignedDocuments#PERSONAL_IDENTIFICATION_NUMBER_AND_NAME personal identification number and name},
     *  {@link IdentifierInSignedDocuments#DATE_OF_BIRTH_AND_NAME date of birth and name} or
     *  {@link IdentifierInSignedDocuments#NAME name only}.
     *  <p>
     *  Not all options are available to every sender, this is detailed in the service's
     *  <a href="https://digipost.github.io/signature-api-specification">functional documentation</a>.
     *
     * @param identifier the identifier type
     */
    B withIdentifierInSignedDocuments(IdentifierInSignedDocuments identifier);

}
