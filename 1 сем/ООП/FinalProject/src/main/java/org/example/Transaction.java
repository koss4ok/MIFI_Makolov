package org.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Transaction {
    public enum Type {
        INCOME,
        EXPENSE
    }

    private final Type type;
    private final double amount;
    private final String category;
    private final String note;
    private final LocalDateTime timestamp;

    @JsonCreator
    public Transaction(@JsonProperty("type") Type type,
                       @JsonProperty("amount") double amount,
                       @JsonProperty("category") String category,
                       @JsonProperty("note") String note,
                       @JsonProperty("timestamp") LocalDateTime timestamp) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.timestamp = timestamp == null ? LocalDateTime.now() : timestamp;
    }

    public static Transaction income(double amount, String category, String note) {
        return new Transaction(Type.INCOME, amount, category, note, LocalDateTime.now());
    }
    public static Transaction expense(double amount, String category, String note) {
        return new Transaction(Type.EXPENSE, amount, category, note, LocalDateTime.now());
    }

    public Type getType() { return type; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getNote() { return note; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
