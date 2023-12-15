package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Lists;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * List Group Repository
 *
 */
@Repository
public interface ListsRepository extends JpaRepository<Lists, Integer> {

}
