package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Quote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Quote Repository
 *
 * @author Amritanshu
 */
@Repository
public interface QuoteRepository extends JpaRepository<Quote, Integer> {

    /**
     * it fetches the Quotes based on customer ECM id
     *
     * @param pCustomerECMId - the customer ECM id
     * @return - List<Quote>
     */
    List<Quote> findByCustomerCustomerECMId(String pCustomerECMId);

    /**
     * it fetches the Quotes based on contact ECM id
     *
     * @param pContactECMId - the contact ECM id
     * @return - List<Quote>
     */
    List<Quote> findByContactContactECMId(String pContactECMId);

}
