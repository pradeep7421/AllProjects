package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Address Repository
 *
 * @author Purushotham Reddy T
 *
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

}
