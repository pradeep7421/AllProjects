package com.winsupply.repository;

import com.winsupply.entity.Order;
import com.winsupply.entity.OrderLine;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * The {@code OrderLineRepository} interface is responsible for interacting with
 * the database to perform CRUD operations on OrderLines entities.
 * @author PRADEEP
 */
public interface OrderLineRepository extends JpaRepository<OrderLine, Integer> {
    /**
     * Retrieve a list of order lines associated with a specific order Id
     *
     * @param pOrder - The order for which to retrieve associated order lines
     * @return A list of order lines related to the specified order
     */
    List<OrderLine> findByOrder(Order pOrder);

    /**
     * Retrieve a list of order lines associated with a specific order Id and
     * orderLine Id
     *
     * @param pOrderLineId - The pOrderLineId for which to retrieve associated order
     *                     lines
     * @param pOrder-      The order for which to retrieve associated order
     * @return A list of Optional type order lines related to the specified order
     */
    Optional<OrderLine> findByOrderLineIdAndOrderOrderId(int pOrderLineId, int pOrderId);

    /**
     * Retrieve orderSequence from orderLine table
     *
     * @return A value of type int
     */
    // by native Query
//    @Query(value = "select max(order_sequence) from order_line o where o.order_id= ?1", nativeQuery = true)
//    Integer getMaxOrderSequence(Integer pOrderId);

    // by jpql
    /**
     * Retrieve orderSequence from orderLine table
     * @param pOrderId- The order Id for which associated max orderSequence is to be
     *                  retrieved from OrderLines
     * @return A value of type integer
     */
    @Query(value = "select max(orderSequence) from OrderLine as o where o.order.orderId=:orderId")
    Integer getMaxOrderSequenceByOrderId(@Param("orderId") Integer pOrderId);

}
