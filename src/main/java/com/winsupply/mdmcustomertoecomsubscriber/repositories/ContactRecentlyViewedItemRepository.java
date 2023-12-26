package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactRecentlyViewedItem;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactRecentlyViewedItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Contact Recently Viewed Item Repository
 *
 * @author Ankit Jain
 *
 */
@Repository
public interface ContactRecentlyViewedItemRepository extends JpaRepository<ContactRecentlyViewedItem, ContactRecentlyViewedItemId> {

    /**
     * <b>deleteAllByIdContactECMId</b> - it deletes the contact recently viewed items
     * based on Contact ECM Id
     *
     * @param pContactEcmId - the Contact ECM Id
     */
    void deleteAllByIdContactECMId(String pContactEcmId);
}
