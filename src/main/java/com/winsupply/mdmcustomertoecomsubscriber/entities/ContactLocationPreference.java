package com.winsupply.mdmcustomertoecomsubscriber.entities;

import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactLocationPreferenceId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Contact Industry Preference
 *
 * @author Purushotham Reddy T
 */
@Entity
@Table(name = "contact_location_preference", schema = "ecom")
@Getter
@Setter
public class ContactLocationPreference {

    @EmbeddedId
    private ContactLocationPreferenceId id;

    @Column(name = "preference_value", length = 25, nullable = false)
    private String preferenceValue;
}
