package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Order Entity
 *
 * @author Ankit Jain
 *
 */
@Entity
@Table(name = "[order]", schema = "ecom")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @OneToOne
    @JoinColumn(name = "contact_ecm_id", referencedColumnName = "contact_ecm_id")
    private Contact contact;

    @OneToOne
    @JoinColumn(name = "approver_contact_ecm_id", referencedColumnName = "contact_ecm_id")
    private Contact approverContact;

    @OneToOne
    @JoinColumn(name = "customer_ecm_id", referencedColumnName = "customer_ecm_id", nullable = false)
    private Customer customer;

}
