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
 * CustomerResupply Id
 *
 * @author Amritanshu
 */
@Embeddable
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResupplyId implements Serializable {

    @Column(name = "customer_ecm_id")
    private String customerECMId;

    @Column(name = "resupply_location")
    private String resupplyLocation;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CustomerResupplyId that = (CustomerResupplyId) o;

        return Objects.equals(customerECMId, that.customerECMId) && Objects.equals(resupplyLocation, that.resupplyLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerECMId, resupplyLocation);
    }
}
