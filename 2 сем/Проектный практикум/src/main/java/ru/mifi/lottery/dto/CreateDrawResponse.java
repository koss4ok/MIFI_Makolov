package ru.mifi.lottery.dto;

import java.util.UUID;

public class CreateDrawResponse {
    public final UUID id;

    public CreateDrawResponse(UUID id) {
        this.id = id;
    }
}
