package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerResupply;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerResupplyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Customer Resupply Repository
 *
 * @author Amritanshu
 */
@Repository
public interface CustomerResupplyRepository extends JpaRepository<CustomerResupply, CustomerResupplyId> {

    /**
     * <b>deleteAllByCustomerECMId</b> - It deletes the CustomerResupply based on
     * Customer ECM Id
     *
     * @param pCustomerECMId - the Customer ECM Id
     */
    @Modifying
    @Query("delete from CustomerResupply cr where cr.id.customerECMId = :customerECMId")
    void deleteAllByCustomerECMId(@Param("customerECMId") String pCustomerECMId);

}
