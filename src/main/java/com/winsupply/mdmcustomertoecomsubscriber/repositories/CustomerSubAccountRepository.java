package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerSubAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Customer Sub Account Repository
 *
 * @author Amritanshu
 *
 */
@Repository
public interface CustomerSubAccountRepository extends JpaRepository<CustomerSubAccount, Integer> {

    @Modifying
    @Query("delete from CustomerSubAccount csa where csa.customer.customerECMId = :customerECMId")
    void deleteAllByCustomerECMId(@Param("customerECMId") String pCustomerECMId);

    List<CustomerSubAccount> findByCustomerCustomerECMIdAndStatusId(@Param("customerECMId") String pCustomerECMId,
            @Param("statusId") Short pStatusId);

    /**
     * it returns the CustomerSubAccount based on customer ECM Id
     *
     * @param pCustomerECMId - the Customer ECM Id
     * @return - List<CustomerSubAccount>
     */
    List<CustomerSubAccount> findByCustomerCustomerECMId(@Param("customerECMId") String pCustomerECMId);

}
