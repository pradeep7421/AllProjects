package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Lists Entity
 *
 * @author Vineetha
 *
 */
@Entity
@Table(name = "list", schema = "ecom")
@Getter
@Setter
public class Lists {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "list_id")
    private Integer listId;

    @ManyToOne
    @JoinColumn(name = "list_group_id", referencedColumnName = "list_group_id", nullable = false)
    private ListGroup listGroup;

    @OneToMany(mappedBy = "list", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ListLine> listLines;

    @OneToMany(mappedBy = "list", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ListToCustomer> listToCustomers;

}
