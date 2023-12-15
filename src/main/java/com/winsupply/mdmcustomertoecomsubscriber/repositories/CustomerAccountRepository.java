package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerAccount;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerAccountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Customer Account Repository
 *
 * @author Amritanshu
 *
 */
@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, CustomerAccountId> {

    /**
     * <b>deleteAllByCustomerECMId</b> - It deletes the CustomerAccount based on
     * Customer ECM Id
     *
     * @param pCustomerECMId - the Customer ECM Id
     */
    @Modifying
    @Query("delete from CustomerAccount ca where ca.id.customerECMId = :customerECMId")
    void deleteAllByCustomerECMId(@Param("customerECMId") String pCustomerECMId);
}
