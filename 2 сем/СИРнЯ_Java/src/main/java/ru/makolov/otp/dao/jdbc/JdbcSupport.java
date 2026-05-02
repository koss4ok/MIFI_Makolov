package ru.makolov.otp.dao.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcSupport {
    private final String url;
    private final String username;
    private final String password;

    public JdbcSupport(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    protected Connection openConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
