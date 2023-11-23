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
 * ListLine Entity
 *
 * @author Vineetha
 *
 */
@Entity
@Table(name = "list_line", schema = "ecom")
@Getter
@Setter
public class ListLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "list_line_id")
    private Integer listLineId;

    @ManyToOne
    @JoinColumn(name = "list_id", referencedColumnName = "list_id", nullable = false)
    private Lists list;

}
