package ru.mifi.lottery.core;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
