package com.winsupply.mdmcustomertoecomsubscriber.entities;

import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ListToCustomerId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * ListToCustomer Entity
 *
 * @author Vineetha
 */
@Entity
@Table(name = "list_to_customer", schema = "ecom")
@Getter
@Setter
public class ListToCustomer {

    @EmbeddedId
    private ListToCustomerId id;

    @ManyToOne
    @JoinColumn(name = "list_id", referencedColumnName = "list_id", nullable = false, insertable = false, updatable = false)
    private Lists list;

}
