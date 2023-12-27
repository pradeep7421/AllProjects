package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactOtherAddress;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactOtherAddressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Contact Other Address Repository
 *
 * @author Ankit Jain
 *
 */
@Repository
public interface ContactOtherAddressRepository extends JpaRepository<ContactOtherAddress, ContactOtherAddressId> {

    /**
     * <b>deleteAllByIdContactECMId</b> - it deletes the Contact Other Address
     * based on Contact ECM Id
     *
     * @param pContactEcmId - the Contact ECM Id
     */
    void deleteAllByIdContactECMId(String pContactEcmId);
}
