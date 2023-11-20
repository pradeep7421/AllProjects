package com.winsupply.entity;

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
 * This {@code OrderLine} class represents entity
 *
 * @author PRADEEP
 */
@Entity
@Table(name = "order_line")
@Getter
@Setter
public class OrderLine {
    /**
     * The orderLineId generates unique values automatically with auto increment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_line_Id")
    private int orderLineId;

    /**
     * The quantity takes values of type int
     */
    @Column(name = "quantity")
    private int quantity;

    /**
     * The itemName takes values of type String
     */
    @Column(name = "item_name", nullable = false)
    private String itemName;

    /**
     * The order used to match with OrderLine using unique Id as order_id
     */
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "order_sequence")
    private int orderSequence;

}
