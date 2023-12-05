package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Contact Role Repository
 *
 * @author Purushotham Reddy T
 *
 */
@Repository
public interface ContactRoleRepository extends JpaRepository<ContactRole, Integer> {

}
