package ru.mifi.lottery.service;

import ru.mifi.lottery.core.Database;
import ru.mifi.lottery.core.NotFoundException;
import ru.mifi.lottery.dto.TicketDto;
import ru.mifi.lottery.model.TicketStatus;
import ru.mifi.lottery.repo.TicketRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public class TicketService {

    private final Database db;
    private final TicketRepository ticketRepo;

    public TicketService(Database db) {
        this.db = db;
        this.ticketRepo = new TicketRepository(db);
    }

    public TicketDto getTicket(UUID ticketId) {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            var row = ticketRepo.getById(conn, ticketId);
            conn.commit();
            if (row == null) {
                throw new NotFoundException("Ticket not found: " + ticketId);
            }

            return new TicketDto(row.id(), row.drawId(), row.status(), toIntegerArray(row.numbers()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Integer[] toIntegerArray(int[] a) {
        Integer[] res = new Integer[a.length];
        for (int i = 0; i < a.length; i++) res[i] = a[i];
        return res;
    }
}
