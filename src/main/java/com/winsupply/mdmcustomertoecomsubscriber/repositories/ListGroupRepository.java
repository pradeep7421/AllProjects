package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.ListGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * List Group Repository
 *
 */
@Repository
public interface ListGroupRepository extends JpaRepository<ListGroup, Integer> {

    /**
     * it fetches the list groups based on customer ECM Id
     *
     * @param pCustomerECMId - the Customer ECM Id
     * @return - List<ListGroup>
     */
    List<ListGroup> findByCustomerCustomerECMId(String pCustomerECMId);
}
