package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactIndustryPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactIndustryPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Contact Industry Preference Repository
 *
 * @author Purushotham Reddy T
 *
 */
@Repository
public interface ContactIndustryPreferenceRepository extends JpaRepository<ContactIndustryPreference, ContactIndustryPreferenceId> {

}
