package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Contact Repository
 *
 * @author Ankit Jain
 *
 */
public interface ContactRepository extends JpaRepository<Contact, String> {

}
