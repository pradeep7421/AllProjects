package com.winsupply.mdmcustomertoecomsubscriber.entities;

import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerAccountId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Customer Account Entity
 *
 * @author Purushotham Reddy T
 *
 */
@Entity
@Table(name = "customer_account", schema = "ecom")
@Getter
@Setter
@Builder
public class CustomerAccount {

    @EmbeddedId
    private CustomerAccountId id;

    @Column(name = "attribute_value")
    private String attributeValue;
}
