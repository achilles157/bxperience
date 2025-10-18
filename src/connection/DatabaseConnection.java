package connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/bxperience";
    private static final String USER = "root"; // ganti jika user database Anda berbeda
    private static final String PASSWORD = ""; // ganti jika Anda memakai password

    private static HikariDataSource dataSource;

    // Blok statis untuk menginisialisasi connection pool saat kelas dimuat
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);

        // Pengaturan optimal untuk performa
        config.setMaximumPoolSize(10); // Jumlah koneksi maksimum di pool
        config.setMinimumIdle(5);      // Jumlah koneksi minimum yang siap sedia
        config.setIdleTimeout(600000); // Waktu (ms) sebelum koneksi idle ditutup
        config.setConnectionTimeout(30000); // Waktu (ms) timeout untuk mendapatkan koneksi

        // Opsi tambahan untuk keandalan
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