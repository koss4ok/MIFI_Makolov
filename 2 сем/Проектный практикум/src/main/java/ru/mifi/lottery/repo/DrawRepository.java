package ru.mifi.lottery.repo;

import ru.mifi.lottery.core.Database;
import ru.mifi.lottery.model.DrawStatus;

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

public class DrawRepository {

    private final Database db;

    public DrawRepository(Database db) {
        this.db = db;
    }

    public UUID create(Connection conn) throws SQLException {
        UUID id = UUID.randomUUID();
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO draws(id,status,created_at) VALUES(?,?,now())")) {
            ps.setObject(1, id);
            ps.setString(2, DrawStatus.ACTIVE.name());
            ps.executeUpdate();
        }
        return id;
    }

    public List<DrawRow> listActive(Connection conn) throws SQLException {
        List<DrawRow> res = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id,status,winning_numbers,created_at,completed_at FROM draws WHERE status='ACTIVE' ORDER BY created_at DESC")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.add(readRow(rs));
                }
            }
        }
        return res;
    }

    public DrawRow lockForCompletion(Connection conn, UUID drawId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id,status,winning_numbers,created_at,completed_at FROM draws WHERE id=? FOR UPDATE")) {
            ps.setObject(1, drawId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return readRow(rs);
            }
        }
    }

    public DrawRow getById(Connection conn, UUID drawId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id,status,winning_numbers,created_at,completed_at FROM draws WHERE id=?")) {
            ps.setObject(1, drawId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return readRow(rs);
            }
        }
    }

    public void markCompleted(Connection conn, UUID drawId, int[] winningNumbers) throws SQLException {
        Array arr = conn.createArrayOf("integer", toIntegerList(winningNumbers).toArray(new Integer[0]));
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE draws SET status='COMPLETED', winning_numbers=?, completed_at=now() WHERE id=?")) {
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

    private static DrawRow readRow(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        DrawStatus status = DrawStatus.valueOf(rs.getString("status"));

        Array array = rs.getArray("winning_numbers");
        int[] winning = null;
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
            winning = tmp;
        }

        Instant createdAt = rs.getTimestamp("created_at").toInstant();
        Timestamp completedAt = rs.getTimestamp("completed_at");
        Instant completed = completedAt == null ? null : completedAt.toInstant();
        return new DrawRow(id, status, winning, createdAt, completed);
    }

    public record DrawRow(UUID id, DrawStatus status, int[] winningNumbers, Instant createdAt, Instant completedAt) {
    }
}
