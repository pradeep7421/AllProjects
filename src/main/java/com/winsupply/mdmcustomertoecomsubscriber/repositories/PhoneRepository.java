package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Phone Repository
 *
 * @author Purushotham Reddy T
 *
 */
@Repository
public interface PhoneRepository extends JpaRepository<Phone, Integer> {


}
