package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Customer Repository
 *
 * @author Ankit Jain
 *
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

}
