package ru.mifi.lottery.api;

import io.javalin.http.Context;
import ru.mifi.lottery.dto.CreateDrawResponse;
import ru.mifi.lottery.dto.DrawDto;
import ru.mifi.lottery.dto.TicketDto;
import ru.mifi.lottery.service.DrawService;

import java.util.List;
import java.util.UUID;

public class DrawController {

    private final DrawService service;

    public DrawController(DrawService service) {
        this.service = service;
    }

    public void createDraw(Context ctx) {
        UUID id = service.createDraw();
        ctx.status(201);
        ctx.json(new CreateDrawResponse(id));
    }

    public void listActiveDraws(Context ctx) {
        List<DrawDto> draws = service.listActiveDraws();
        ctx.json(draws);
    }

    public void createTicket(Context ctx) {
        UUID drawId = UUID.fromString(ctx.pathParam("id"));
        TicketDto ticket = service.createTicket(drawId);
        ctx.status(201);
        ctx.json(ticket);
    }

    public void completeDraw(Context ctx) {
        UUID drawId = UUID.fromString(ctx.pathParam("id"));
        DrawDto draw = service.completeDraw(drawId);
        ctx.json(draw);
    }
}
