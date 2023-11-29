package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactEmailPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactEmailPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Contact Email Preference Repository
 *
 * @author Purushotham Reddy T
 */
public interface ContactEmailPreferenceRepository extends JpaRepository<ContactEmailPreference, ContactEmailPreferenceId> {

}
