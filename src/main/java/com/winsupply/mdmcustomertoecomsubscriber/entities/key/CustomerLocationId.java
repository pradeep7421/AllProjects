package com.winsupply.mdmcustomertoecomsubscriber.entities.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * Customer Location ID
 *
 * @author Purushotham Reddy T
 */
@Embeddable
@Setter
@Getter
public class CustomerLocationId implements Serializable {

    @Column(name = "customer_ecm_id")
    private String customerEcmId;

    @Column(name = "company_number")
    private String companyNumber;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomerLocationId that = (CustomerLocationId) o;

        if (!customerEcmId.equals(that.customerEcmId)) return false;
        return companyNumber.equals(that.companyNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerEcmId, companyNumber);
    }

}
