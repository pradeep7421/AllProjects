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
 * CustomerAccountNumber Id
 *
 * @author Amritanshu
 */
@Embeddable
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAccountNumberId implements Serializable {

    @Column(name = "customer_ecm_id")
    private String customerECMId;

    @Column(name = "account_number")
    private String accountNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CustomerAccountNumberId that = (CustomerAccountNumberId) o;

        return Objects.equals(customerECMId, that.customerECMId) && Objects.equals(accountNumber, that.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerECMId, accountNumber);
    }
}
