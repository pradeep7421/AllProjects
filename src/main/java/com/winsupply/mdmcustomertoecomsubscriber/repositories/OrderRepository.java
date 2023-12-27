package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Order Repository
 *
 * @author Ankit Jain
 *
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    /**
     * <b>findAllByContactContactECMId</b> - it finds the Orders based on Contact ECM Id
     *
     * @param pContactECMId - the Contact ECM Id
     * @return - List<Order>
     */
    List<Order> findAllByContactContactECMId(String pContactECMId);

    /**
     * <b>findAllByApproverContactContactECMId</b> - it finds the Orders based on Approver Contact ECM Id
     *
     * @param pContactECMId - the Contact ECM Id
     * @return - List<Order>
     */
    List<Order> findAllByApproverContactContactECMId(String pContactECMId);

    /**
     * <b>findAllByCustomerCustomerECMId</b> - it finds the Orders based on Customer ECM Id
     *
     * @param pCustomerECMId - the Customer ECM Id
     * @return - List<Order>
     */
    List<Order> findAllByCustomerCustomerECMId(String pCustomerECMId);
}
