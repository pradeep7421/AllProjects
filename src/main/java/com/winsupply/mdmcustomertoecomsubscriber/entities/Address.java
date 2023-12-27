package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Address Entity
 *
 * @author Ankit Jain
 *
 */
@Entity
@Table(name = "address", schema = "ecom")
@Getter
@Setter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer id;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "address1", length = 200)
    private String address1;

    @Column(name = "address2", length = 100)
    private String address2;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "state", length = 10)
    private String state;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "image_url", length = 80)
    private String imageUrl;

    @Column(name = "latitude", precision = 8, scale = 6)
    private Double latitude;

    @Column(name = "longitude", precision = 6, scale = 6)
    private Double longitude;

    @OneToMany(mappedBy = "address", cascade = CascadeType.REMOVE)
    private Set<Phone> phoneNumbers;

    @OneToMany(mappedBy = "address", cascade = CascadeType.REMOVE)
    private Set<OrderEmailAddress> orderEmailAddresses;
}
