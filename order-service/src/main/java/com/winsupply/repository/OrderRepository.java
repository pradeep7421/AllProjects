package com.winsupply.repository;

import com.winsupply.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * The repository interface for managing Order entities in the database. 
 *
 * @param <Order>   - The entity type, representing an Order in the database
 * @param <Integer> - The data type of the primary key for the Order entity
 * @author PRADEEP
 */
public interface OrderRepository extends JpaRepository<Order, Integer> {
    /**
     * Retrieve ALL orders from order table through search term
     * @param pSearchTerm- The Search Term with which all orders are retrieved
     * @param pPageable- The pageable which carries information for searching Operation
     * @return A Object of type Page<Order>
     */
    @Query("SELECT o FROM Order o WHERE (CAST(o.orderId AS string) = :searchTerm OR o.orderName LIKE %:searchTerm%) ")
//    @Query("SELECT o FROM Order o WHERE o.orderId like :searchTerm or o.orderName like %:searchTerm% ")
    Page<Order> findAllOrderBySearchTerm(@Param("searchTerm") String pSearchTerm, Pageable pPageable);

    /**
     * Retrieve ALL orders from order table through searchterm
     * @param pSearchTerm- The Search Term with which all orders are retrieved
     * @param pPageable- The pageable which carries information for searching Operation
     * @return A Object of type Page<Order>
     */
//    @Query("SELECT o FROM Order o WHERE o.orderName LIKE %:searchTerm%")
//    Page<Order> findAllOrderBySearchTermString(@Param("searchTerm") String pSearchTerm, Pageable pPageable);

}
