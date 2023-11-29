package com.winsupply.mdmcustomertoecomsubscriber.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Industry
 *
 * @author Purushotham Reddy T
 */
@Entity
@Table(name = "industry", schema = "ecom")
@Getter
@Setter
public class Industry {
    @Id
    @Column(name = "industry_id")
    private Short industryId;

    @Column(name = "industry_desc", length = 25, nullable = false)
    private String industryDesc;
}
