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
 * Customer Account Key
 *
 * @author Purushotham Reddy T
 */
@Embeddable
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAccountId implements Serializable {

    @Column(name = "customer_ecm_id")
    private String customerECMId;

    @Column(name = "company_number")
    private String companyNumber;

    @Column(name = "attribute_name")
    private String attributeName;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CustomerAccountId that = (CustomerAccountId) o;

        return Objects.equals(customerECMId, that.customerECMId) && Objects.equals(companyNumber, that.companyNumber)
                && Objects.equals(attributeName, that.attributeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerECMId, companyNumber, attributeName);
    }
}
