package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.ListGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * List Group Repository
 *
 */
@Repository
public interface ListGroupRepository extends JpaRepository<ListGroup, Integer> {

}
