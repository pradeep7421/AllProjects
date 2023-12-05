package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.EmailPreference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * Email Preference Repository
 *
 * @author Purushotham Reddy T
 */
public interface EmailPreferenceRepository extends JpaRepository<EmailPreference, String> {

    Optional<EmailPreference> findByEmailPreferenceDesc(@Param("emailPreferenceDesc") String pEmailPreferenceDesc);
}
