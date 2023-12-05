package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Contact;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Contact Repository
 *
 * @author Ankit Jain
 *
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, String> {

    Optional<Contact> findByLogin(@Param("email") String pEmail);

    List<Contact> findByCustomerCustomerECMIdIn(List<String> pOldCustomerIds);

}
