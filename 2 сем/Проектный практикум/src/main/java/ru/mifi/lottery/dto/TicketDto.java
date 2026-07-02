package ru.mifi.lottery.dto;

import ru.mifi.lottery.model.TicketStatus;
import java.util.UUID;

public class TicketDto {
    public UUID id;
    public UUID drawId;
    public TicketStatus status;
    public Integer[] numbers;

    public TicketDto() {
    }

    public TicketDto(UUID id, UUID drawId, TicketStatus status, Integer[] numbers) {
        this.id = id;
        this.drawId = drawId;
        this.status = status;
        this.numbers = numbers;
    }
}
