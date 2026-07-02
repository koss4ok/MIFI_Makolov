package ru.mifi.lottery.api;

import io.javalin.http.Context;
import ru.mifi.lottery.dto.TicketDto;
import ru.mifi.lottery.service.TicketService;

import java.util.UUID;

public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    public void getTicket(Context ctx) {
        UUID ticketId = UUID.fromString(ctx.pathParam("id"));
        TicketDto ticket = service.getTicket(ticketId);
        ctx.json(ticket);
    }
}
