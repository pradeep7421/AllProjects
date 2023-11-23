package com.winsupply.mdmcustomertoecomsubscriber.entities.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * Customer Account Key
 *
 * @author Purushotham Reddy T
 */
@Embeddable
@Setter
@Getter
public class CustomerAccountId implements Serializable {

    @Column(name = "customer_ecm_id")
    private String customerEcmId;

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

        return Objects.equals(customerEcmId, that.customerEcmId) && Objects.equals(companyNumber, that.companyNumber)
                && Objects.equals(attributeName, that.attributeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerEcmId, companyNumber, attributeName);
    }
}
