package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Email Preference
 *
 * @author Purushotham Reddy T
 */
@Entity
@Table(name = "email_preference", schema = "ecom")
@Getter
@Setter
public class EmailPreference {
    @Id
    @Column(name = "email_preference_id")
    private Short emailPreferenceId;

    @Column(name = "email_preference_desc", length = 40, nullable = false)
    private String emailPreferenceDesc;
}
