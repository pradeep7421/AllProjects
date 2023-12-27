package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Address Entity
 *
 * @author Ankit Jain
 *
 */
@Entity
@Table(name = "phone_number", schema = "ecom")
@Getter
@Setter
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phone_number_id")
    private Integer id;

    @Column(name = "phone_number", length = 10, nullable = false)
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "phone_number_type_id")
    private PhoneNumberType phoneNumberType;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;
}
