package com.winsupply.mdmcustomertoecomsubscriber.entities.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * Contact Email Preference Id
 *
 * @author Purushotham Reddy T
 */
@Embeddable
@Getter
@Setter
public class ContactEmailPreferenceId implements Serializable {


    @Column(name = "email_preference_id")
    private Short emailPreferenceId;

    @Column(name = "contact_ecm_id", length = 40, nullable = false)
    private String contactEcmId;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContactEmailPreferenceId that = (ContactEmailPreferenceId) o;

        if (!emailPreferenceId.equals(that.emailPreferenceId)) return false;
        return contactEcmId.equals(that.contactEcmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactEcmId, emailPreferenceId);
    }

}
