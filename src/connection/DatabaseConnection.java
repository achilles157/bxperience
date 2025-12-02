package connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/consolerent";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(600000);
        config.setConnectionTimeout(30000);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            dataSource = new HikariDataSource(config);
            System.out.println("Connection pool berhasil diinisialisasi.");
        } catch (Exception e) {
            System.err.println("Gagal menginisialisasi connection pool: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mengambil koneksi dari connection pool.
     * 
     * @return Connection object dari pool.
     * @throws SQLException jika gagal mendapatkan koneksi.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Connection pool belum diinisialisasi.");
        }
        System.out.println("Mengambil koneksi dari pool...");
        return dataSource.getConnection();
    }

    /**
     * Menutup seluruh connection pool. Panggil ini saat aplikasi ditutup.
     */
    public static void closeConnectionPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Connection pool ditutup.");
        }
    }
}