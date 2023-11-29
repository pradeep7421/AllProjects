package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Contact Repository
 *
 * @author Ankit Jain
 *
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, String> {

    Optional<Contact> findByEmail(@Param("email") String pEmail);


}
