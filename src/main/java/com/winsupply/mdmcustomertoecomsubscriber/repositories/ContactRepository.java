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

    /**
     * <b>findByLogin</b> - it finds the Contact based on email id
     *
     * @param pEmail - the Email
     * @return - Optional<Contact>
     */
    Optional<Contact> findByLogin(@Param("email") String pEmail);

    /**
     * <b>findByCustomerCustomerECMId</b> - It finds the list of contact based on
     * Customer ECM Id
     *
     * @param pCustomerECMId - the Customer ECM Id
     * @return - List<Contact>
     */
    List<Contact> findByCustomerCustomerECMId(String pCustomerECMId);

}
