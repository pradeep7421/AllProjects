package com.winsupply.mdmcustomertoecomsubscriber.entities;

import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactIndustryPreferenceId;
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
@Table(name = "contact_industry_preference", schema = "ecom")
@Getter
@Setter
public class ContactIndustryPreference {

    @EmbeddedId
    private ContactIndustryPreferenceId id;


}
