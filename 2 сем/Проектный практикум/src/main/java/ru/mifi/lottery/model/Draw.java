package ru.mifi.lottery.model;

import java.time.Instant;
import java.util.UUID;

public class Draw {
    public UUID id;
    public DrawStatus status;
    public int[] winningNumbers;
    public Instant createdAt;
    public Instant completedAt;
}
