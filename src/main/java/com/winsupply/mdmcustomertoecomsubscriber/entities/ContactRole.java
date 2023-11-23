package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Contact Role Entity
 *
 * @author Ankit Jain
 *
 */
@Entity
@Table(name = "contact_role", schema = "ecom")
@Getter
@Setter
public class ContactRole {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_desc", length = 30, nullable = false)
    private String roleDesc;

    @OneToMany
    @JoinTable(schema = "ecom", name = "contact_role_permission", joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<ContactPermission> permissions;
}
