package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.ListToCustomer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ListToCustomerId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListToCustomerRepository extends JpaRepository<ListToCustomer, ListToCustomerId> {

    /**
     * <b>findByIdCustomerECMId</b> - It returns List of List to customer based on
     * Customer ECM Id
     *
     * @param pCustomerECMId - the Customer ECM Id
     * @return - List<ListToCustomer>
     */
    List<ListToCustomer> findByIdCustomerECMId(String pCustomerECMId);

}
