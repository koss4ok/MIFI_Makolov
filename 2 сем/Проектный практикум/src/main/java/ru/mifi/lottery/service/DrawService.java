package ru.mifi.lottery.service;

import ru.mifi.lottery.core.AppConfig;
import ru.mifi.lottery.core.ConflictException;
import ru.mifi.lottery.core.Database;
import ru.mifi.lottery.core.NotFoundException;
import ru.mifi.lottery.dto.DrawDto;
import ru.mifi.lottery.dto.TicketDto;
import ru.mifi.lottery.model.DrawStatus;
import ru.mifi.lottery.repo.DrawRepository;
import ru.mifi.lottery.repo.TicketRepository;
import ru.mifi.lottery.util.CombinationGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class DrawService {

    private final Database db;
    private final AppConfig cfg;
    private final DrawRepository drawRepo;
    private final TicketRepository ticketRepo;

    public DrawService(Database db, AppConfig cfg) {
        this.db = db;
        this.cfg = cfg;
        this.drawRepo = new DrawRepository(db);
        this.ticketRepo = new TicketRepository(db);
    }

    public UUID createDraw() {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            UUID id = drawRepo.create(conn);
            conn.commit();
            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<DrawDto> listActiveDraws() {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            List<DrawRepository.DrawRow> rows = drawRepo.listActive(conn);
            conn.commit();
            return rows.stream().map(this::toDto).toList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public TicketDto createTicket(UUID drawId) {
        for (int attempt = 0; attempt < 5; attempt++) {
            int[] numbers = CombinationGenerator.generateUniqueNumbers(cfg.drawNumCount(), cfg.drawNumMin(), cfg.drawNumMax());
            try (Connection conn = db.getConnection()) {
                conn.setAutoCommit(false);
                DrawRepository.DrawRow draw = drawRepo.lockForCompletion(conn, drawId);
                if (draw == null) {
                    throw new NotFoundException("Draw not found: " + drawId);
                }
                if (draw.status() != DrawStatus.ACTIVE) {
                    throw new ConflictException("Cannot create ticket for completed draw");
                }

                UUID ticketId;
                try {
                    ticketId = ticketRepo.create(conn, drawId, numbers);
                } catch (SQLException e) {
                    if ("23505".equals(e.getSQLState())) {
                        conn.rollback();
                        continue;
                    }
                    throw e;
                }

                var row = ticketRepo.getById(conn, ticketId);
                conn.commit();

                return new TicketDto(row.id(), row.drawId(), row.status(), toIntegerArray(row.numbers()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        throw new ConflictException("Unable to create ticket due to repeated number collision");
    }

    public DrawDto completeDraw(UUID drawId) {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            DrawRepository.DrawRow draw = drawRepo.lockForCompletion(conn, drawId);
            if (draw == null) {
                throw new NotFoundException("Draw not found: " + drawId);
            }
            if (draw.status() != DrawStatus.ACTIVE) {
                throw new ConflictException("Draw already completed");
            }

            int[] winning = CombinationGenerator.generateUniqueNumbers(cfg.drawNumCount(), cfg.drawNumMin(), cfg.drawNumMax());
            drawRepo.markCompleted(conn, drawId, winning);
            ticketRepo.updateStatuses(conn, drawId, winning);

            conn.commit();

            DrawRepository.DrawRow completed = drawRepo.getById(conn, drawId);
            return toDto(completed, winning);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private DrawDto toDto(DrawRepository.DrawRow row) {
        return toDto(row, row.winningNumbers());
    }

    private DrawDto toDto(DrawRepository.DrawRow row, int[] winningNumbersOverride) {
        Integer[] win = null;
        int[] winSrc = winningNumbersOverride;
        if (winSrc != null) {
            win = toIntegerArray(winSrc);
        }
        return new DrawDto(row.id(), row.status(), win);
    }

    private static Integer[] toIntegerArray(int[] a) {
        if (a == null) return null;
        Integer[] res = new Integer[a.length];
        for (int i = 0; i < a.length; i++) res[i] = a[i];
        return res;
    }
}
