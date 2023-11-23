package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * ListGroup Entity
 *
 * @author Vineetha
 *
 */
@Entity
@Table(name = "list_group", schema = "ecom")
@Getter
@Setter
@Builder
public class ListGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "list_group_id")
    private Integer groupId;

    @OneToOne
    @JoinColumn(name = "contact_ecm_id", referencedColumnName = "contact_ecm_id")
    private Contact contact;

    @OneToOne
    @JoinColumn(name = "customer_ecm_id", referencedColumnName = "customer_ecm_id")
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "company_number", referencedColumnName = "company_number")
    private Location location;

    @OneToMany(mappedBy = "listGroup", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private java.util.List<Lists> lists;

}
