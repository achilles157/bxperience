package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/bxperience";
    private static final String USER = "root"; // ganti jika user database Anda berbeda
    private static final String PASSWORD = ""; // ganti jika Anda memakai password

    private static Connection connection = null;

    public static Connection getConnection() {
    try {
        if (connection == null || connection.isClosed()) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Koneksi ke database berhasil.");
        }
    } catch (ClassNotFoundException e) {
        System.err.println("Driver JDBC tidak ditemukan: " + e.getMessage());
    } catch (SQLException e) {
        System.err.println("Koneksi gagal: " + e.getMessage());
    }
    return connection;
}


    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Koneksi ditutup.");
            } catch (SQLException e) {
                System.err.println("Gagal menutup koneksi: " + e.getMessage());
            }
        }
    }
}
