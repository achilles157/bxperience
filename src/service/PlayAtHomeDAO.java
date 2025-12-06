package service;

import connection.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for Play At Home rental operations.
 * Handles retrieving categories, checking stock availability, and creating
 * rental transactions.
 */
public class PlayAtHomeDAO {

    /**
     * Retrieves all unique asset categories.
     * 
     * @return List of category names
     * @throws SQLException if a database error occurs
     */
    public List<String> getCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT kategori FROM aset";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("kategori"));
            }
        }
        return categories;
    }

    /**
     * Retrieves available item names for a specific category.
     * 
     * @param category Asset category
     * @return List of available item names
     * @throws SQLException if a database error occurs
     */
    public List<String> getAvailableItemNames(String category) throws SQLException {
        List<String> items = new ArrayList<>();
        String query = "SELECT DISTINCT nama_barang FROM aset WHERE kategori = ? AND status_tersedia = 1 AND status_disewakan = 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, category);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    items.add(rs.getString("nama_barang"));
                }
            }
        }
        return items;
    }

    /**
     * Gets the total count of available items for a category.
     * 
     * @param category Asset category
     * @return Total count of available items
     * @throws SQLException if a database error occurs
     */
    public int getAvailableItemCount(String category) throws SQLException {
        String query = "SELECT COUNT(id_aset) as total FROM aset WHERE kategori = ? AND status_tersedia = 1 AND status_disewakan = 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, category);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    /**
     * Gets a map of available item counts grouped by item name for a category.
     * 
     * @param category Asset category
     * @return Map where key is item name and value is available count
     * @throws SQLException if a database error occurs
     */
    public Map<String, Integer> getAvailableItemCountsByName(String category) throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        String query = "SELECT nama_barang, COUNT(*) as total FROM aset WHERE kategori = ? AND status_tersedia = 1 AND status_disewakan = 1 GROUP BY nama_barang";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, category);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("nama_barang"), rs.getInt("total"));
                }
            }
        }
        return counts;
    }

    /**
     * Kelas helper untuk menyimpan informasi stok dan harga item.
     */
    public static class ItemStockInfo {
        public int totalAvailable;
        public double pricePerDay;

        public ItemStockInfo(int totalAvailable, double pricePerDay) {
            this.totalAvailable = totalAvailable;
            this.pricePerDay = pricePerDay;
        }
    }

    /**
     * Retrieves stock information (total available and price) for a specific item.
     * 
     * @param itemName Name of the item
     * @return ItemStockInfo object containing total available and price per day
     * @throws SQLException if a database error occurs
     */
    public ItemStockInfo getItemStockInfo(String itemName) throws SQLException {
        String query = "SELECT COUNT(*) as total, MIN(harga_sewa_hari) as harga FROM aset WHERE nama_barang = ? AND status_tersedia = 1 AND status_disewakan = 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, itemName);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new ItemStockInfo(rs.getInt("total"), rs.getDouble("harga"));
                }
            }
        }
        return new ItemStockInfo(0, 0.0);
    }

    /**
     * Creates a new Play At Home rental transaction.
     * Handles inserting header, details, and updating asset availability status.
     *
     * @param nama          Renter's name
     * @param lokasi        Renter's location
     * @param instagram     Instagram handle
     * @param tglMulai      Start date
     * @param tglSelesai    End date
     * @param metode        Pickup method
     * @param alamatAntar   Delivery address
     * @param alamatKembali Return address
     * @param keperluan     Purpose of rental
     * @param ongkir        Shipping cost
     * @param totalHarga    Total price
     * @param items         List of items to rent
     * @return true if successful
     * @throws SQLException if a database error occurs
     */
    public boolean createRental(String nama, String alamatLengkap, String noTelp, java.sql.Date tglMulai,
            java.sql.Date tglSelesai,
            String metode, String alamatAntar, String alamatKembali, String keperluan,
            String namaKurir, String noTelpKurir,
            double ongkir, double diskon, double totalHarga, List<RentalItem> items) throws SQLException {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert Header
            String insertPlayHome = "INSERT INTO playathome (nama, alamat_lengkap, no_telp, tgl_mulai, tgl_selesai, metode_pengambilan, alamat_antar, alamat_kembali, keperluan, nama_kurir, no_telp_kurir, ongkir, diskon, total_harga, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'aktif')";
            int idPlayhome = 0;

            try (PreparedStatement pst = conn.prepareStatement(insertPlayHome, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, nama);
                pst.setString(2, alamatLengkap);
                pst.setString(3, noTelp);
                pst.setDate(4, tglMulai);
                pst.setDate(5, tglSelesai);
                pst.setString(6, metode);
                pst.setString(7, alamatAntar);
                pst.setString(8, alamatKembali);
                pst.setString(9, keperluan);
                pst.setString(10, namaKurir);
                pst.setString(11, noTelpKurir);
                pst.setDouble(12, ongkir);
                pst.setDouble(13, diskon);
                pst.setDouble(14, totalHarga);
                pst.executeUpdate();

                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    idPlayhome = generatedKeys.getInt(1);
                }
            }

            // 2. Insert Details and Update Asset Status
            for (RentalItem item : items) {
                // Get available asset IDs
                String idQuery = "SELECT id_aset FROM aset WHERE nama_barang = ? AND status_tersedia = 1 AND status_disewakan = 1 LIMIT ?";
                List<String> assetIds = new ArrayList<>();

                try (PreparedStatement getId = conn.prepareStatement(idQuery)) {
                    getId.setString(1, item.itemName);
                    getId.setInt(2, item.quantity);
                    try (ResultSet idRs = getId.executeQuery()) {
                        while (idRs.next()) {
                            assetIds.add(idRs.getString("id_aset"));
                        }
                    }
                }

                if (assetIds.size() < item.quantity) {
                    throw new SQLException("Stok barang " + item.itemName + " tidak mencukupi.");
                }

                try (PreparedStatement detailStmt = conn.prepareStatement(
                        "INSERT INTO playathome_detail (id_playhome, id_aset, jumlah, subtotal) VALUES (?, ?, ?, ?)");
                        PreparedStatement updateStmt = conn.prepareStatement(
                                "UPDATE aset SET status_tersedia = 0 WHERE id_aset = ?")) {

                    for (String idAset : assetIds) {
                        // Calculate duration in days
                        long diff = tglSelesai.getTime() - tglMulai.getTime();
                        long days = java.util.concurrent.TimeUnit.DAYS.convert(diff,
                                java.util.concurrent.TimeUnit.MILLISECONDS);
                        if (days < 1)
                            days = 1; // Minimum 1 day rental

                        // Insert detail
                        detailStmt.setInt(1, idPlayhome);
                        detailStmt.setString(2, idAset);
                        detailStmt.setInt(3, 1);
                        double subtotal = item.pricePerDay * days;
                        detailStmt.setDouble(4, subtotal);
                        detailStmt.executeUpdate();

                        // Update asset status
                        updateStmt.setString(1, idAset);
                        updateStmt.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Automatically completes rentals that have passed their end date.
     * Updates asset status to available (1) and rental status to 'selesai'.
     */
    public void autoCompleteRentals() {
        String selectExpired = "SELECT id_playhome FROM playathome " +
                "WHERE status = 'aktif' AND tgl_selesai < CURDATE()";

        String updateRental = "UPDATE playathome SET status = 'selesai' WHERE id_playhome = ?";
        String getDetails = "SELECT id_aset FROM playathome_detail WHERE id_playhome = ?";
        String updateAsset = "UPDATE aset SET status_tersedia = 1 WHERE id_aset = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Integer> expiredIds = new ArrayList<>();
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(selectExpired)) {
                while (rs.next()) {
                    expiredIds.add(rs.getInt("id_playhome"));
                }
            }

            if (expiredIds.isEmpty())
                return;

            conn.setAutoCommit(false);
            try (PreparedStatement pstRental = conn.prepareStatement(updateRental);
                    PreparedStatement pstDetails = conn.prepareStatement(getDetails);
                    PreparedStatement pstAsset = conn.prepareStatement(updateAsset)) {

                for (int idRental : expiredIds) {
                    // Update Rental Status
                    pstRental.setInt(1, idRental);
                    pstRental.executeUpdate();

                    // Get Assets and make them available
                    pstDetails.setInt(1, idRental);
                    try (ResultSet rsAssets = pstDetails.executeQuery()) {
                        while (rsAssets.next()) {
                            String idAset = rsAssets.getString("id_aset");
                            pstAsset.setString(1, idAset);
                            pstAsset.addBatch();
                        }
                    }
                }
                pstAsset.executeBatch();
                conn.commit();
                System.out.println("Auto-completed " + expiredIds.size() + " playathome rentals.");
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Kelas helper untuk merepresentasikan item yang akan disewa dalam transaksi.
     */
    public static class RentalItem {
        public String itemName;
        public int quantity;
        public double pricePerDay;

        public RentalItem(String itemName, int quantity, double pricePerDay) {
            this.itemName = itemName;
            this.quantity = quantity;
            this.pricePerDay = pricePerDay;
        }
    }
}
