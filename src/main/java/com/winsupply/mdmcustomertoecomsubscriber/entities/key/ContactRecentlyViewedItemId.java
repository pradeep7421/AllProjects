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
 * Contact Recently Viewed Item Id
 *
 * @author Ankit Jain
 */
@Embeddable
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactRecentlyViewedItemId implements Serializable {

    @Column(name = "contact_ecm_id", length = 15, nullable = false)
    private String contactECMId;

    @Column(name = "wise_item_number", length = 20, nullable = false)
    private String wiseItemNumber;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ContactRecentlyViewedItemId other = (ContactRecentlyViewedItemId) obj;
        return Objects.equals(contactECMId, other.contactECMId) && Objects.equals(wiseItemNumber, other.wiseItemNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactECMId, wiseItemNumber);
    }
}
