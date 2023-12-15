package com.winsupply.mdmcustomertoecomsubscriber.repositories;

import com.winsupply.mdmcustomertoecomsubscriber.entities.Industry;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Industry Repository
 *
 * @author Purushotham Reddy T
 *
 */
@Repository
public interface IndustryRepository extends JpaRepository<Industry, Short> {

    /**
     * <b>findByIndustryDesc</b> - it finds the Industry based on Industry
     * description
     *
     * @param pIndustryDesc - the Industry description
     * @return - Optional<Industry>
     */
    Optional<Industry> findByIndustryDescIgnoreCase(@Param("industryDesc") String pIndustryDesc);
}
