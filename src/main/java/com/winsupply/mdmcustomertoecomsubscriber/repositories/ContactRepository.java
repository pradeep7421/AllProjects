package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Contact Repository
 *
 * @author Ankit Jain
 *
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, String> {

}
