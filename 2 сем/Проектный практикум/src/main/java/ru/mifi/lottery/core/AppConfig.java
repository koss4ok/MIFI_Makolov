package ru.mifi.lottery.core;

public class AppConfig {

    private final int port;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private final int drawNumCount;
    private final int drawNumMin;
    private final int drawNumMax;

    private AppConfig(int port, String dbUrl, String dbUser, String dbPassword,
                       int drawNumCount, int drawNumMin, int drawNumMax) {
        this.port = port;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.drawNumCount = drawNumCount;
        this.drawNumMin = drawNumMin;
        this.drawNumMax = drawNumMax;
    }

    public static AppConfig fromEnv() {
        int port = envInt("PORT", 8080);
        String dbUrl = requiredEnv("DB_URL");
        String dbUser = requiredEnv("DB_USER");
        String dbPassword = requiredEnv("DB_PASSWORD");

        int drawNumCount = envInt("DRAW_NUM_COUNT", 6);
        int drawNumMin = envInt("DRAW_NUM_MIN", 1);
        int drawNumMax = envInt("DRAW_NUM_MAX", 49);

        if (drawNumCount <= 0) {
            throw new IllegalArgumentException("DRAW_NUM_COUNT must be > 0");
        }
        if (drawNumMin >= drawNumMax) {
            throw new IllegalArgumentException("DRAW_NUM_MIN must be < DRAW_NUM_MAX");
        }
        if ((drawNumMax - drawNumMin + 1) < drawNumCount) {
            throw new IllegalArgumentException("Range too small for DRAW_NUM_COUNT");
        }

        return new AppConfig(port, dbUrl, dbUser, dbPassword, drawNumCount, drawNumMin, drawNumMax);
    }

    private static String requiredEnv(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Missing env var: " + key);
        }
        return v;
    }

    private static int envInt(String key, int def) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) return def;
        return Integer.parseInt(v.trim());
    }

    public int port() {
        return port;
    }

    public String dbUrl() {
        return dbUrl;
    }

    public String dbUser() {
        return dbUser;
    }

    public String dbPassword() {
        return dbPassword;
    }

    public int drawNumCount() {
        return drawNumCount;
    }

    public int drawNumMin() {
        return drawNumMin;
    }

    public int drawNumMax() {
        return drawNumMax;
    }
}
