package com.winsupply.mdmcustomertoecomsubscriber.entities;

import com.winsupply.mdmcustomertoecomsubscriber.entities.key.CustomerAccountKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
public class CustomerAccount {

    @EmbeddedId
    private CustomerAccountKey id;

    @Column(name = "attribute_value")
    private String attributeValue;
}
