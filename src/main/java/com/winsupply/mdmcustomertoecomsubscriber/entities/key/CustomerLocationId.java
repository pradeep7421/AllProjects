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
 * Customer Location ID
 *
 * @author Purushotham Reddy T
 */
@Embeddable
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLocationId implements Serializable {

    @Column(name = "customer_ecm_id")
    private String customerECMId;

    @Column(name = "company_number")
    private String companyNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CustomerLocationId that = (CustomerLocationId) o;

        if (!customerECMId.equals(that.customerECMId))
            return false;
        return companyNumber.equals(that.companyNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerECMId, companyNumber);
    }

}
