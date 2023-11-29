package com.winsupply.mdmcustomertoecomsubscriber.entities;

import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactEmailPreferenceId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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

    @Column(name = "date_time_added")
    private LocalDateTime dateTimeAdded;

    @Column(name = "date_time_last_updated")
    private LocalDateTime dateTimeLastUpdated;
    @PrePersist
    protected void onCreate() {
        dateTimeAdded = LocalDateTime.now();
        dateTimeLastUpdated = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        dateTimeLastUpdated = LocalDateTime.now();
    }
}
