package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactLocationPreference;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactLocationPreferenceId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Contact Location Preference Repository
 *
 * @author Ankit Jain
 *
 */
@Repository
public interface ContactLocationPreferenceRepository extends JpaRepository<ContactLocationPreference, ContactLocationPreferenceId> {

    /**
     * <b>deleteAllByIdContactECMId</b> - it deletes the Contact Location Preference
     * based on Contact ECM Id
     *
     * @param pContactEcmId - the Contact ECM Id
     */
    void deleteAllByIdContactECMId(String pContactEcmId);

    /**
     * <b>findByIdContactECMIdAndIdPreferenceName</b> - It finds the
     * ContactLocationPreference based on contact ECM Id and preference name
     *
     * @param pContactECMId   - the Contact ECM Id
     * @param pPreferenceName - the Preference Name
     * @return - List<ContactLocationPreference>
     */
    List<ContactLocationPreference> findByIdContactECMIdAndIdPreferenceName(String pContactECMId, String pPreferenceName);
}
