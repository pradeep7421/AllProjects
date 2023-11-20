package com.winsupply.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * This {@code Order} class represents entity
 *
 * @author PRADEEP
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    /**
     * The orderId generates unique values automatically with auto increment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id")
    private int orderId;

    /**
     * The amount takes values of type double.
     */
    @Column(name = "amount")
    private double amount;

    /**
     * The orderName takes values which are strings
     */
    @Column(name = "order_name", nullable = false)
    private String orderName;

    /**
     * The orderLines retrieves list of orderLines from OrderLines entity
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE)
    private List<OrderLine> orderLines;

}
