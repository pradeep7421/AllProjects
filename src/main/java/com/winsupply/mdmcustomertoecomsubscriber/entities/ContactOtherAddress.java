package com.winsupply.mdmcustomertoecomsubscriber.entities;

import com.winsupply.mdmcustomertoecomsubscriber.entities.key.ContactOtherAddressId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Contact Other Address
 *
 * @author Ankit Jain
 */
@Entity
@Table(name = "contact_other_address", schema = "ecom")
@Getter
@Setter
public class ContactOtherAddress {

    @EmbeddedId
    private ContactOtherAddressId id;
}
