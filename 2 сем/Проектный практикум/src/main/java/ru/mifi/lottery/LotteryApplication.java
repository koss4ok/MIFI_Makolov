package ru.mifi.lottery;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import ru.mifi.lottery.api.DrawController;
import ru.mifi.lottery.api.TicketController;
import ru.mifi.lottery.core.AppConfig;
import ru.mifi.lottery.core.Database;
import ru.mifi.lottery.core.ErrorResponse;
import ru.mifi.lottery.service.DrawService;
import ru.mifi.lottery.service.TicketService;

public class LotteryApplication {

    public static void main(String[] args) {
        AppConfig cfg = AppConfig.fromEnv();
        Database db = new Database(cfg);
        DrawService drawService = new DrawService(db, cfg);
        TicketService ticketService = new TicketService(db);

        Javalin app = Javalin.create(config -> {
        }).start(cfg.port());

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResponse(e.getMessage()));
        });
        app.exception(ru.mifi.lottery.core.NotFoundException.class, (e, ctx) -> {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(new ErrorResponse(e.getMessage()));
        });
        app.exception(ru.mifi.lottery.core.ConflictException.class, (e, ctx) -> {
            ctx.status(HttpStatus.CONFLICT);
            ctx.json(new ErrorResponse(e.getMessage()));
        });
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(new ErrorResponse("Internal server error"));
        });

        DrawController drawController = new DrawController(drawService);
        TicketController ticketController = new TicketController(ticketService);

        app.post("/draws", drawController::createDraw);
        app.get("/draws", drawController::listActiveDraws);
        app.post("/draws/{id}/tickets", drawController::createTicket);
        app.post("/draws/{id}/complete", drawController::completeDraw);

        app.get("/tickets/{id}", ticketController::getTicket);

        Runtime.getRuntime().addShutdownHook(new Thread(db::close));
    }
}
