package ru.mifi.lottery.dto;

import ru.mifi.lottery.model.DrawStatus;
import java.util.UUID;

public class DrawDto {
    public UUID id;
    public DrawStatus status;
    public Integer[] winningNumbers;

    public DrawDto() {
    }

    public DrawDto(UUID id, DrawStatus status, Integer[] winningNumbers) {
        this.id = id;
        this.status = status;
        this.winningNumbers = winningNumbers;
    }
}
