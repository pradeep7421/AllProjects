package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Industry Repository
 *
 * @author Purushotham Reddy T
 *
 */
@Repository
public interface IndustryRepository extends JpaRepository<Industry, Short> {
    Optional<Industry> findByIndustryDesc(@Param("industryDesc") String pIndustryDesc);


}
