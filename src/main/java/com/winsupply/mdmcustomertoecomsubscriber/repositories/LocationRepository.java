package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Location Repository
 *
 * @author Ankit Jain
 *
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, String> {

}
