package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Customer Repository
 *
 * @author Ankit Jain
 *
 */
public interface CustomerRepository extends JpaRepository<Customer, String> {

}
