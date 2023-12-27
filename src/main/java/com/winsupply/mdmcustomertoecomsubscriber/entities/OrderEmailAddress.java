package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Order Email Address
 *
 * @author Purushotham Reddy T
 */
@Entity
@Table(name = "order_email_address", schema = "ecom")
@Getter
@Setter
public class OrderEmailAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_email_address_id")
    private Integer orderEmailAddressId;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(name = "order_email_address", length = 250, nullable = false)
    private String orderEmailAddress;
}
