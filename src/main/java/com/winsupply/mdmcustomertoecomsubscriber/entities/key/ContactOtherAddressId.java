package com.winsupply.mdmcustomertoecomsubscriber.entities.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contact Other Address Id
 *
 * @author Ankit Jain
 */
@Embeddable
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactOtherAddressId implements Serializable {

    @Column(name = "contact_ecm_id", length = 15, nullable = false)
    private String contactECMId;

    @Column(name = "address_id", nullable = false)
    private Integer addressId;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ContactOtherAddressId other = (ContactOtherAddressId) obj;
        return Objects.equals(addressId, other.addressId) && Objects.equals(contactECMId, other.contactECMId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressId, contactECMId);
    }
}
