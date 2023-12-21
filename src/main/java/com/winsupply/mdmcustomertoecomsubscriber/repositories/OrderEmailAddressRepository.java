package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.OrderEmailAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Order Email Address Repository
 *
 * @author Purushotham Reddy T
 *
 */
@Repository
public interface OrderEmailAddressRepository extends JpaRepository<OrderEmailAddress, Integer> {

    /**
     * <b>deleteAllByAddressId</b> - It delete phone based on address
     *
     * @param pAddressId - the Address Id
     */
    void deleteAllByAddressId(Integer pAddressId);
}
