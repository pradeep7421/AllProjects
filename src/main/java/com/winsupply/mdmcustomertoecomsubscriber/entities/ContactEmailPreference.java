package com.winsupply.mdmcustomertoecomsubscriber.entities;

import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactEmailPreferenceId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Contact Email Preference
 *
 * @author Purushotham Reddy T
 */
@Entity
@Table(name = "contact_email_preference", schema = "ecom")
@Getter
@Setter
public class ContactEmailPreference {
    @EmbeddedId
    private ContactEmailPreferenceId id;

}
