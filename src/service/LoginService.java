package service;

import connection.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {

    /**
     * Melakukan verifikasi login berdasarkan username dan password
     * @param username input dari pengguna
     * @param password input dari pengguna
     * @return true jika login berhasil, false jika gagal
     */
    public static boolean login(String username, String password) {
        String query = "SELECT * FROM user_login WHERE username = ? AND password = ?";
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            return rs.next(); // jika ada hasil, login berhasil
        } catch (SQLException e) {
            System.err.println("Login gagal: " + e.getMessage());
            return false;
        }
    }
}
