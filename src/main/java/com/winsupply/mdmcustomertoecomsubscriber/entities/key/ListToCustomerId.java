package com.winsupply.mdmcustomertoecomsubscriber.entities.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ListToCustomerId implements Serializable {

    @Column(name = "list_id")
    private Integer listId;

    @Column(name = "customer_ecm_id")
    private String customerECMId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListToCustomerId that = (ListToCustomerId) o;

        if (!listId.equals(that.listId)) return false;
        return customerECMId.equals(that.customerECMId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerECMId, listId);
    }

}
