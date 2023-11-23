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
public class CustomerAccountKey implements Serializable {

    @Column(name = "customer_ecm_id", length = 15)
    private String customerEcmId;

    @Column(name = "company_number", length = 5)
    private String companyNumber;

    @Column(name = "attribute_name", length = 40)
    private String attributeName;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CustomerAccountKey that = (CustomerAccountKey) o;

        return Objects.equals(customerEcmId, that.customerEcmId) && Objects.equals(companyNumber, that.companyNumber)
                && Objects.equals(attributeName, that.attributeName);
    }

    public int hashCode() {
        return Objects.hash(customerEcmId, companyNumber, attributeName);
    }
}
