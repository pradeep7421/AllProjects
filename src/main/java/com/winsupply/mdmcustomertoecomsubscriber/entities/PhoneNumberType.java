package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Phone Number Type Entity
 *
 * @author Purushotham Reddy T
 *
 */
@Entity
@Table(name = "phone_number_type", schema = "ecom")
@Getter
@Setter
public class PhoneNumberType {

    @Id
    @Column(name = "phone_number_type_id")
    private Short phoneNumberTypeId;

    @Column(name = "phone_number_type_desc", length = 20, nullable = false)
    private String phoneNumberTypeDesc;

    @Column(name = "phone_number_type_sort", length = 10, nullable = false)
    private String phoneNumberTypeSort;

}
