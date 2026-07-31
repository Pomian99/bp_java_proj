package org.example.model;

import java.io.Serializable;

/**
 * The person placing an Order.
 */
public record Customer(String name, String email, String address) implements Serializable {
}
