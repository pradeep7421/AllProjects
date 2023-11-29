package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.PhoneNumberType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Phone Type Repository
 *
 * @author Purushotham Reddy T
 *
 */
@Repository
public interface PhoneTypeRepository extends JpaRepository<PhoneNumberType, Short> {

    Optional<PhoneNumberType> findByPhoneNumberTypeDesc(String pPhoneNumberTypeDesc);
}
