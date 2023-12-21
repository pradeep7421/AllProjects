package com.winsupply.mdmcustomertoecomsubscriber.entities;

import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactRecentlyViewedItemId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * CContact Recently Viewed Item
 *
 * @author Ankit Jain
 */
@Entity
@Table(name = "contact_recently_viewed_item", schema = "ecom")
@Getter
@Setter
public class ContactRecentlyViewedItem {

    @EmbeddedId
    private ContactRecentlyViewedItemId id;

    @Column(name = "order_sequence", length = 5, nullable = false)
    private String orderSequence;
}
