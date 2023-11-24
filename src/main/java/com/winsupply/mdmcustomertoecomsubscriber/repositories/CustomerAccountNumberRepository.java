package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerAccountNumber;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerAccountNumberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Customer Account Number Repository
 *
 * @author Amritanshu
 */
@Repository
public interface CustomerAccountNumberRepository extends JpaRepository<CustomerAccountNumber, CustomerAccountNumberId> {

    @Modifying
    @Query("delete from CustomerAccountNumber can where can.id.customerECMId = :customerECMId")
    void deleteAllByCustomerECMId(@Param("customerECMId") String pCustomerECMId);

}
