package ru.mifi.lottery.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class Database {

    private final HikariDataSource ds;

    public Database(AppConfig cfg) {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(cfg.dbUrl());
        hc.setUsername(cfg.dbUser());
        hc.setPassword(cfg.dbPassword());
        hc.setMaximumPoolSize(10);
        hc.setAutoCommit(false);
        this.ds = new HikariDataSource(hc);

        runMigrations();
    }

    private void runMigrations() {
        String resource = "db/migration/V1__init.sql";
        try (Connection conn = ds.getConnection();
             InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Migration resource not found: " + resource);
            }

            conn.setAutoCommit(true);
            String sql = readAll(in);
            try (Statement st = conn.createStatement()) {
                for (String stmt : sql.split(";")) {
                    String s = stmt.trim();
                    if (!s.isEmpty()) {
                        st.execute(s);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Migration failed", e);
        }
    }

    private static String readAll(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void close() {
        ds.close();
    }
}
