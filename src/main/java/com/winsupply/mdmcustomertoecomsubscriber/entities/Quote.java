package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Quote Entity
 *
 * @author Ankit Jain
 *
 */
@Entity
@Table(name = "quote", schema = "ecom")
@Getter
@Setter
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quote_id", nullable = false)
    private Integer quoteId;

    @OneToOne
    @JoinColumn(name = "contact_ecm_id", referencedColumnName = "contact_ecm_id")
    private Contact contact;

    @OneToOne
    @JoinColumn(name = "customer_ecm_id", referencedColumnName = "customer_ecm_id", nullable = false)
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "company_number", referencedColumnName = "company_number", nullable = false)
    private Location location;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.REMOVE)
    private Set<QuoteAttachment> quoteAttachments;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.REMOVE)
    private List<QuoteLine> quoteLines;

}
