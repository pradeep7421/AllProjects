package com.winsupply.mdmcustomertoecomsubscriber.entities;

import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerLocationId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Customer Location
 *
 * @author Purushotham Reddy T
 */
@Entity
@Table(name = "customer_location", schema = "ecom")
@Getter
@Setter
@Builder
public class CustomerLocation {

    @EmbeddedId
    private CustomerLocationId id;

}
