package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Customer Repository
 *
 * @author Ankit Jain
 *
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    @Modifying
    @Query("delete from Customer cr where cr.customerECMId = :customerECMId")
    void deleteByCustomerECMId(@Param("customerECMId") String pCustomerECMId);

}
