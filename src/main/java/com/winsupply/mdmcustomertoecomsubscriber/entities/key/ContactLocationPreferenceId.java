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
 * Contact Location Preference Id
 *
 * @author Ankit Jain
 */
@Embeddable
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactLocationPreferenceId implements Serializable {

    @Column(name = "contact_ecm_id", length = 15, nullable = false)
    private String contactECMId;

    @Column(name = "company_number", length = 5, nullable = false)
    private String companyNumber;

    @Column(name = "preference_name", length = 25, nullable = false)
    private String preferenceName;

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj)
            return true;
        if (pObj == null)
            return false;
        if (getClass() != pObj.getClass())
            return false;
        ContactLocationPreferenceId other = (ContactLocationPreferenceId) pObj;
        return Objects.equals(companyNumber, other.companyNumber) && Objects.equals(contactECMId, other.contactECMId)
                && Objects.equals(preferenceName, other.preferenceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyNumber, contactECMId, preferenceName);
    }
}
