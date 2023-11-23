package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Contact Permission Entity
 *
 * @author Ankit Jain
 *
 */
@Entity
@Table(name = "contact_permission", schema = "ecom")
@Getter
@Setter
public class ContactPermission {

    @Id
    @Column(name = "permission_id")
    private Integer permissionId;

    @Column(name = "permission_desc", length = 50, nullable = false)
    private String permissionDesc;
}
