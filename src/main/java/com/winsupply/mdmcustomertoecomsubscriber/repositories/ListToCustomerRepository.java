package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.ListToCustomer;
import com.winsupply.mdmcustomertoecomsubscriber.entities.Lists;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ListToCustomerId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ListToCustomerRepository extends JpaRepository<ListToCustomer, ListToCustomerId> {

    List<ListToCustomer> findByIdCustomerECMId(String pCustomerECMId);

}
