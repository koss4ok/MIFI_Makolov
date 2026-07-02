package ru.mifi.lottery.repo;

import ru.mifi.lottery.core.Database;
import ru.mifi.lottery.model.TicketStatus;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TicketRepository {

    private final Database db;

    public TicketRepository(Database db) {
        this.db = db;
    }

    public UUID create(Connection conn, UUID drawId, int[] numbers) throws SQLException {
        UUID id = UUID.randomUUID();
        Array arr = conn.createArrayOf("integer", toIntegerList(numbers).toArray(new Integer[0]));
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO tickets(id,draw_id,status,numbers,created_at) VALUES(?,?,?,?,now())")) {
            ps.setObject(1, id);
            ps.setObject(2, drawId);
            ps.setString(3, TicketStatus.PENDING.name());
            ps.setArray(4, arr);
            ps.executeUpdate();
        }
        return id;
    }

    public TicketRow getById(Connection conn, UUID ticketId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id,draw_id,status,numbers,created_at FROM tickets WHERE id=?")) {
            ps.setObject(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return readRow(rs);
            }
        }
    }

    public List<TicketRow> listByDraw(Connection conn, UUID drawId) throws SQLException {
        List<TicketRow> res = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id,draw_id,status,numbers,created_at FROM tickets WHERE draw_id=? ORDER BY created_at DESC")) {
            ps.setObject(1, drawId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.add(readRow(rs));
                }
            }
        }
        return res;
    }

    public void updateStatuses(Connection conn, UUID drawId, int[] winningNumbers) throws SQLException {
        Array arr = conn.createArrayOf("integer", toIntegerList(winningNumbers).toArray(new Integer[0]));
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tickets SET status = CASE WHEN numbers = ? THEN 'WIN' ELSE 'LOSE' END WHERE draw_id=?")) {
            ps.setArray(1, arr);
            ps.setObject(2, drawId);
            ps.executeUpdate();
        }
    }

    private static List<Integer> toIntegerList(int[] a) {
        List<Integer> res = new ArrayList<>(a.length);
        for (int v : a) res.add(v);
        return res;
    }

    private static TicketRow readRow(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        UUID drawId = (UUID) rs.getObject("draw_id");
        TicketStatus status = TicketStatus.valueOf(rs.getString("status"));

        Array array = rs.getArray("numbers");
        int[] nums = null;
        if (array != null) {
            Object obj = array.getArray();
            int[] tmp;
            if (obj instanceof Integer[]) {
                Integer[] ints = (Integer[]) obj;
                tmp = new int[ints.length];
                for (int i = 0; i < ints.length; i++) tmp[i] = ints[i];
            } else if (obj instanceof int[]) {
                tmp = (int[]) obj;
            } else {
                throw new SQLException("Unexpected array type: " + obj.getClass());
            }
            nums = tmp;
        } else {
            nums = new int[0];
        }

        Timestamp createdAtTs = rs.getTimestamp("created_at");
        Instant createdAt = createdAtTs.toInstant();

        return new TicketRow(id, drawId, status, nums, createdAt);
    }

    public record TicketRow(UUID id, UUID drawId, TicketStatus status, int[] numbers, Instant createdAt) {
    }
}
