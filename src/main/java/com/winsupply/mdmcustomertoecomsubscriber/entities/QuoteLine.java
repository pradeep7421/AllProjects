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
 * Quote Line Entity
 *
 * @author Ankit Jain
 *
 */
@Entity
@Table(name = "quote_line", schema = "ecom")
@Getter
@Setter
public class QuoteLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quote_line_id")
    private Long quoteLineId;

    @ManyToOne
    @JoinColumn(name = "quote_id")
    private Quote quote;
}
