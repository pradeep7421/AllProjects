package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Quote Repository
 *
 * @author Amritanshu
 */
@Repository
public interface QuoteRepository extends JpaRepository<Quote, Integer> {

}
