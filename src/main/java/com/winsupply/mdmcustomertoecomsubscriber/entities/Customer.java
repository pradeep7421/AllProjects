package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Customer Entity
 *
 * @author Ankit Jain
 *
 */
@Entity
@Table(name = "customer", schema = "ecom")
@Getter
@Setter
public class Customer {

    @Id
    @Column(name = "customer_ecm_id", length = 15)
    private String customerECMId;

    @Column(name = "customer_name", length = 120, nullable = false)
    private String customerName;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "default_billing_address_id", referencedColumnName = "address_id")
    private Address defaultBillingAddress;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "default_shipping_address_id", referencedColumnName = "address_id")
    private Address defaultShippingAddress;

    @Column(name = "federal_tax_id", length = 15)
    private String federalTaxId;

    @Column(name = "email", length = 80)
    private String email;

    @Column(name = "phone", length = 10)
    private String phone;

    @Column(name = "wincca", length = 20)
    private String wincca;

    @OneToOne
    @JoinColumn(name = "win_default_company", referencedColumnName = "company_number")
    private Location winDefaultCompany;

}
