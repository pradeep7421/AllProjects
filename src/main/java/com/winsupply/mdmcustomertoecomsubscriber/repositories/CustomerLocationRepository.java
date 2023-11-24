package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerLocation;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerLocationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Customer Location Repository
 *
 * @author Amritanshu
 */
@Repository
public interface CustomerLocationRepository extends JpaRepository<CustomerLocation, CustomerLocationId> {

    @Modifying
    @Query("delete from CustomerLocation cl where cl.id.customerECMId = :customerECMId")
    void deleteAllByCustomerECMId(@Param("customerECMId") String pCustomerECMId);

}
