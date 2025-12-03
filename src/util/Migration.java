package util;

import connection.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class Migration {

    public static void main(String[] args) {
        migrate();
    }

    public static void migrate() {
        System.out.println("Starting migration...");
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // 1. PlayAtHome: Rename instagram -> no_telp
            try {
                System.out.println("Renaming instagram to no_telp in playathome...");
                stmt.executeUpdate("ALTER TABLE playathome CHANGE instagram no_telp VARCHAR(20)");
                System.out.println("Success.");
            } catch (SQLException e) {
                System.out.println("Skipped (maybe already exists): " + e.getMessage());
            }

            // 2. PlayAtHome: Rename lokasi -> alamat_lengkap
            try {
                System.out.println("Renaming lokasi to alamat_lengkap in playathome...");
                stmt.executeUpdate("ALTER TABLE playathome CHANGE lokasi alamat_lengkap TEXT");
                System.out.println("Success.");
            } catch (SQLException e) {
                System.out.println("Skipped (maybe already exists): " + e.getMessage());
            }

            // 3. PlayAtHome: Add nama_kurir
            try {
                System.out.println("Adding nama_kurir to playathome...");
                stmt.executeUpdate("ALTER TABLE playathome ADD nama_kurir VARCHAR(100)");
                System.out.println("Success.");
            } catch (SQLException e) {
                System.out.println("Skipped (maybe already exists): " + e.getMessage());
            }

            // 4. PlayAtHome: Add no_telp_kurir
            try {
                System.out.println("Adding no_telp_kurir to playathome...");
                stmt.executeUpdate("ALTER TABLE playathome ADD no_telp_kurir VARCHAR(20)");
                System.out.println("Success.");
            } catch (SQLException e) {
                System.out.println("Skipped (maybe already exists): " + e.getMessage());
            }

            // 5. PlayAtHome: Add diskon
            try {
                System.out.println("Adding diskon to playathome...");
                stmt.executeUpdate("ALTER TABLE playathome ADD diskon DECIMAL(10,2) DEFAULT 0");
                System.out.println("Success.");
            } catch (SQLException e) {
                System.out.println("Skipped (maybe already exists): " + e.getMessage());
            }

            // 6. Booking: Add diskon
            try {
                System.out.println("Adding diskon to booking...");
                stmt.executeUpdate("ALTER TABLE booking ADD diskon DECIMAL(10,2) DEFAULT 0");
                System.out.println("Success.");
            } catch (SQLException e) {
                System.out.println("Skipped (maybe already exists): " + e.getMessage());
            }

            System.out.println("Migration completed.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
