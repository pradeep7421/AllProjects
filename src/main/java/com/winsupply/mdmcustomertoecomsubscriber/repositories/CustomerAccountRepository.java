package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.CustomerAccount;
import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerAccountKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Customer Account Repository
 *
 * @author Purushotham Reddy T
 *
 */
@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, CustomerAccountKey> {

}
