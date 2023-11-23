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
 * Quote Attachment Entity
 *
 * @author Ankit Jain
 *
 */
@Entity
@Table(name = "quote_attachment", schema = "ecom")
@Getter
@Setter
public class QuoteAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Integer attachmentId;

    @ManyToOne
    @JoinColumn(name = "quote_id")
    private Quote quote;
}
