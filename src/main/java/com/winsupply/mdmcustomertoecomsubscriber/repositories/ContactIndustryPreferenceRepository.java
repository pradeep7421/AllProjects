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

    /**
     * <b>deleteByIdContactEcmId</b> - it deletes the Contact Industry Preference
     * based on Contact ECM Id
     *
     * @param pContactEcmId - the Contact ECM Id
     */
    void deleteAllByIdContactEcmId(String pContactEcmId);
}
