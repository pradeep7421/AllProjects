package com.winsupply.mdmcustomertoecomsubscriber.entities;

import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerResupplyId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Customer Resupply
 *
 * @author Amritanshu
 *
 */
@Entity
@Table(name = "customer_resupply_location", schema = "ecom")
@Getter
@Setter
@Builder
public class CustomerResupply {

    @EmbeddedId
    private CustomerResupplyId id;
}
