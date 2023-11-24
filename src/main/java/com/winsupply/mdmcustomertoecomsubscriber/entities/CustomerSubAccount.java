package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * CustomerSubAccount Entity
 *
 * @author Amritanshu
 *
 */
@Entity
@Table(name = "customer_sub_account", schema = "ecom")
@Getter
@Setter
public class CustomerSubAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_account_id")
    private Integer subAccountId;

    @Column(name = "sub_account_name", length = 80, nullable = false)
    private String subAccountName;

    @OneToOne
    @JoinColumn(name = "company_number", referencedColumnName = "company_number", nullable = false)
    private Location companyNumber;

    @OneToOne
    @JoinColumn(name = "customer_ecm_id", referencedColumnName = "customer_ecm_id", nullable = false)
    private Customer customer;

    @Column(name = "status_id", nullable = false)
    private Short statusId;

    @Column(name = "account_number", length = 10, nullable = false)
    private String accountNumber;

    @Column(name = "freight_percent", precision = 3, scale = 2)
    private BigDecimal freightPercent;

    @Column(name = "freight_cost", precision = 9, scale = 4)
    private BigDecimal freightCost;

    @Column(name = "purchase_order_required_code", length = 1)
    private String poRequiredCode;

    @Column(name = "credit_status_code", length = 2)
    private String creditStatusCode;

    @OneToOne
    @JoinColumn(name = "customer_address", referencedColumnName = "address_id", nullable = false)
    private Address customerAddress;

}
