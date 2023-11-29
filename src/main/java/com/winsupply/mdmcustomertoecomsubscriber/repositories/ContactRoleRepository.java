package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.ContactRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Contact Role Repository
 *
 * @author Purushotham Reddy T
 *
 */
@Repository
public interface ContactRoleRepository extends JpaRepository<ContactRole, String> {
    Optional<ContactRole> findByRoleDesc(@Param("roleDesc") String pRoleDesc);


}
