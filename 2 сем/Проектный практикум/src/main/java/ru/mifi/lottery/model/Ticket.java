package ru.mifi.lottery.model;

import java.time.Instant;
import java.util.UUID;

public class Ticket {
    public UUID id;
    public UUID drawId;
    public TicketStatus status;
    public int[] numbers;
    public Instant createdAt;
}
