package com.winsupply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * {@code OrderServiceApplication} is the entry for the application It is used
 * run the application.
 * @author PRADEEP
 */
@SpringBootApplication
public class OrderServiceApplication {

    /**
     * The main method that starts this application
     *
     * @param args Command-line arguments provided to the main method
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
