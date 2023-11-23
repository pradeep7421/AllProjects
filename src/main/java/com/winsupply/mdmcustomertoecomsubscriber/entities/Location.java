package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Location Entity
 *
 * @author Ankit Jain
 *
 */
@Entity
@Table(name = "location", schema = "ecom")
@Getter
@Setter
public class Location {

    @Id
    @Column(name = "company_number")
    private String companyNumber;

}
