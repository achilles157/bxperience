package service;

import connection.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Booking operations.
 * Handles all database interactions related to bookings, including availability
 * checks,
 * price calculations, and booking creation.
 */
public class BookingDAO {

    /**
     * Gets the price per minute for a specific category.
     * 
     * @param category The asset category
     * @return Price per minute, or 0 if not found
     * @throws SQLException if a database error occurs
     */
    public double getPricePerMinute(String category) throws SQLException {
        String query = "SELECT MIN(harga_sewa_menit) as harga FROM aset WHERE kategori = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, category);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("harga");
                }
            }
        }
        return 0;
    }

    /**
     * Retrieves all unique asset categories that are marked as rentable.
     * 
     * @return List of all rentable categories
     * @throws SQLException if a database error occurs
     */
    /**
     * Retrieves all unique asset categories that are marked as rentable, filtered
     * by area.
     * 
     * @param area "Regular" or "VIP Room"
     * @return List of all rentable categories in the area
     * @throws SQLException if a database error occurs
     */
    public List<String> getAllCategoriesByArea(String area) throws SQLException {
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT kategori FROM aset WHERE kategori LIKE ? ORDER BY kategori";
        String param = area.equalsIgnoreCase("VIP Room") ? "%VIP%" : "%";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, param);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String cat = rs.getString("kategori");
                    // Manual filtering for Regular to exclude VIP if needed,
                    // but for now let's assume VIP categories contain "VIP"
                    if (area.equalsIgnoreCase("Regular")) {
                        if (!cat.toUpperCase().contains("VIP")) {
                            categories.add(cat);
                        }
                    } else {
                        // VIP Area
                        if (cat.toUpperCase().contains("VIP")) {
                            categories.add(cat);
                        }
                    }
                }
            }
        }
        return categories;
    }

    /**
     * Retrieves a list of available experience categories for a given time slot and
     * area.
     * 
     * @param date            The booking date
     * @param time            The booking time (HH:mm:ss)
     * @param durationMinutes Duration in minutes
     * @param area            "Regular" or "VIP Room"
     * @return List of available categories
     * @throws SQLException if a database error occurs
     */
    public List<String> getAvailableExperiencesByArea(java.sql.Date date, String time, int durationMinutes, String area)
            throws SQLException {
        List<String> availableCategories = new ArrayList<>();
        String query = "SELECT DISTINCT a.kategori FROM aset a " +
                "WHERE a.status_tersedia = 1 " +
                "AND a.id_aset NOT IN (" +
                "    SELECT bd.id_aset " +
                "    FROM booking b " +
                "    JOIN booking_detail bd ON b.id_booking = bd.id_booking " +
                "    WHERE b.tanggal = ? " +
                "    AND b.status != 'cancelled' " +
                "    AND (" +
                "        b.jam < ADDTIME(TIME(?), SEC_TO_TIME(? * 60)) " +
                "        AND " +
                "        ADDTIME(b.jam, SEC_TO_TIME(b.durasi_menit * 60)) > TIME(?)" +
                "    )" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setDate(1, date);
            pst.setString(2, time);
            pst.setInt(3, durationMinutes);
            pst.setString(4, time);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String cat = rs.getString("kategori");
                    if (area.equalsIgnoreCase("Regular")) {
                        if (!cat.toUpperCase().contains("VIP")) {
                            availableCategories.add(cat);
                        }
                    } else {
                        // VIP Area
                        if (cat.toUpperCase().contains("VIP")) {
                            availableCategories.add(cat);
                        }
                    }
                }
            }
        }
        return availableCategories;
    }

    /**
     * Checks the number of available assets for a specific category and time slot.
     * 
     * @param category        The asset category
     * @param date            The booking date
     * @param time            The booking time
     * @param durationMinutes Duration in minutes
     * @return Number of available assets
     * @throws SQLException if a database error occurs
     */
    /**
     * Memeriksa jumlah aset yang tersedia untuk kategori dan waktu tertentu.
     *
     * @param category        Kategori aset.
     * @param date            Tanggal booking.
     * @param time            Waktu mulai booking.
     * @param durationMinutes Durasi sewa dalam menit.
     * @return Jumlah aset yang tersedia.
     * @throws SQLException jika terjadi kesalahan database.
     */
    public int checkAvailabilityCount(String category, java.sql.Date date, String time, int durationMinutes)
            throws SQLException {
        String query = "SELECT COUNT(*) as available FROM aset a " +
                "WHERE a.kategori = ? AND a.status_tersedia = 1 " +
                "AND NOT EXISTS (" +
                "  SELECT 1 FROM booking b " +
                "  JOIN booking_detail bd ON b.id_booking = bd.id_booking " +
                "  WHERE bd.id_aset = a.id_aset " +
                "  AND b.tanggal = ? " +
                "  AND ( " +
                "    b.jam < ADDTIME(TIME(?), SEC_TO_TIME(? * 60)) " +
                "    AND " +
                "    ADDTIME(b.jam, SEC_TO_TIME(b.durasi_menit * 60)) > TIME(?) " +
                "  )" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, category);
            pst.setDate(2, date);
            pst.setString(3, time);
            pst.setInt(4, durationMinutes);
            pst.setString(5, time);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("available");
                }
            }
        }
        return 0;
    }

    /**
     * Creates a new booking transaction.
     * 
     * @param bookingData Object containing booking details (simplified as params
     *                    for now)
     * @return true if successful
     * @throws SQLException if a database error occurs
     */
    /**
     * Membuat transaksi booking baru.
     * Melakukan pengecekan ketersediaan aset sebelum menyimpan data booking.
     *
     * @param nama            Nama penyewa.
     * @param noHp            Nomor HP penyewa.
     * @param date            Tanggal booking.
     * @param time            Waktu mulai booking.
     * @param durationMinutes Durasi sewa dalam menit.
     * @param category        Kategori aset yang disewa.
     * @param quantity        Jumlah unit yang disewa.
     * @param extraConsole    Status apakah menyewa konsol tambahan (untuk VIP).
     * @return true jika booking berhasil dibuat, false jika gagal.
     * @throws SQLException jika terjadi kesalahan database atau aset tidak cukup.
     */
    public boolean createBooking(String nama, String noHp,
            java.sql.Date date, String time, int durationMinutes,
            String category, int quantity, boolean extraConsole, double diskon) throws SQLException {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String checkQuery = "SELECT COUNT(*) as available FROM aset a " +
                    "WHERE a.kategori = ? AND a.status_tersedia = 1 " +
                    "AND NOT EXISTS (" +
                    "  SELECT 1 FROM booking b " +
                    "  JOIN booking_detail bd ON b.id_booking = bd.id_booking " +
                    "  WHERE bd.id_aset = a.id_aset " +
                    "  AND b.tanggal = ? " +
                    "  AND ( " +
                    "    b.jam < ADDTIME(TIME(?), SEC_TO_TIME(? * 60)) " +
                    "    AND " +
                    "    ADDTIME(b.jam, SEC_TO_TIME(b.durasi_menit * 60)) > TIME(?) " +
                    "  )" +
                    ")";

            int checkCount = 0;
            try (PreparedStatement pstCheck = conn.prepareStatement(checkQuery)) {
                pstCheck.setString(1, category);
                pstCheck.setDate(2, date);
                pstCheck.setString(3, time);
                pstCheck.setInt(4, durationMinutes);
                pstCheck.setString(5, time);
                ResultSet rs = pstCheck.executeQuery();
                if (rs.next())
                    checkCount = rs.getInt("available");
            }

            if (checkCount < quantity) {
                throw new SQLException("Tidak cukup aset tersedia. Tersedia: " + checkCount);
            }

            double pricePerMinute = 0;
            String priceQuery = "SELECT MIN(harga_sewa_menit) as harga FROM aset WHERE kategori = ?";
            try (PreparedStatement pstPrice = conn.prepareStatement(priceQuery)) {
                pstPrice.setString(1, category);
                ResultSet rsPrice = pstPrice.executeQuery();
                if (rsPrice.next())
                    pricePerMinute = rsPrice.getDouble("harga");
            }

            double totalHarga = Math.ceil(pricePerMinute * durationMinutes * quantity);
            if (extraConsole) {
                totalHarga += (15000 * quantity);
            }

            // Apply discount
            totalHarga -= diskon;
            if (totalHarga < 0)
                totalHarga = 0;

            String insertBooking = "INSERT INTO booking (nama, no_hp, tanggal, jam, durasi_menit, total_harga, diskon, status) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'confirmed')";

            int bookingId = 0;
            try (PreparedStatement pstBooking = conn.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS)) {
                pstBooking.setString(1, nama);
                pstBooking.setString(2, noHp);
                pstBooking.setDate(3, date);
                pstBooking.setString(4, time);
                pstBooking.setInt(5, durationMinutes);
                pstBooking.setDouble(6, totalHarga);
                pstBooking.setDouble(7, diskon);
                pstBooking.executeUpdate();

                ResultSet generatedKeys = pstBooking.getGeneratedKeys();
                if (generatedKeys.next()) {
                    bookingId = generatedKeys.getInt(1);
                }
            }

            String getAssetsQuery = "SELECT a.id_aset FROM aset a " +
                    "WHERE a.kategori = ? AND a.status_tersedia = 1 " +
                    "AND NOT EXISTS (" +
                    "  SELECT 1 FROM booking b " +
                    "  JOIN booking_detail bd ON b.id_booking = bd.id_booking " +
                    "  WHERE bd.id_aset = a.id_aset " +
                    "  AND b.tanggal = ? " +
                    "  AND ( " +
                    "    b.jam < ADDTIME(TIME(?), SEC_TO_TIME(? * 60)) " +
                    "    AND " +
                    "    ADDTIME(b.jam, SEC_TO_TIME(b.durasi_menit * 60)) > TIME(?) " +
                    "  )" +
                    ") LIMIT ?";

            String insertDetail = "INSERT INTO booking_detail (id_booking, id_aset, jumlah, add_on, subtotal) VALUES (?, ?, 1, ?, ?)";
            String updateAssetStatus = "UPDATE aset SET status_tersedia = 0 WHERE id_aset = ?";

            try (PreparedStatement pstAssets = conn.prepareStatement(getAssetsQuery);
                    PreparedStatement pstDetail = conn.prepareStatement(insertDetail);
                    PreparedStatement pstUpdateStatus = conn.prepareStatement(updateAssetStatus)) {

                pstAssets.setString(1, category);
                pstAssets.setDate(2, date);
                pstAssets.setString(3, time);
                pstAssets.setInt(4, durationMinutes);
                pstAssets.setString(5, time);
                pstAssets.setInt(6, quantity);

                ResultSet rsAssets = pstAssets.executeQuery();
                while (rsAssets.next()) {
                    String assetId = rsAssets.getString("id_aset");
                    pstDetail.setInt(1, bookingId);
                    pstDetail.setString(2, assetId);
                    pstDetail.setInt(3, extraConsole ? 1 : 0);
                    pstDetail.setDouble(4, pricePerMinute * durationMinutes); // Subtotal per unit (excluding extra fee
                                                                              // for now, or should I include it?)
                    // Usually subtotal is per line item. If extra fee is global, maybe keep it
                    // separate.
                    // But if I want to track it per unit, I should probably add it here too?
                    // Let's keep subtotal as base price for now to match previous logic, or add it?
                    // If I add it, it matches totalHarga.
                    // Let's add it to subtotal for consistency.
                    double subtotal = (pricePerMinute * durationMinutes);
                    if (extraConsole)
                        subtotal += 15000;
                    pstDetail.setDouble(4, subtotal);

                    pstDetail.addBatch();

                    // Update asset status
                    pstUpdateStatus.setString(1, assetId);
                    pstUpdateStatus.addBatch();
                }
                pstDetail.executeBatch();
                pstUpdateStatus.executeBatch();
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
     * Automatically completes bookings that have passed their end time.
     * Updates asset status to available (1) and booking status to 'completed'.
     */
    public void autoCompleteBookings() {
        String selectExpired = "SELECT id_booking FROM booking " +
                "WHERE status IN ('confirmed', 'pending') " +
                "AND TIMESTAMP(tanggal, jam) + INTERVAL durasi_menit MINUTE < NOW()";

        String updateBooking = "UPDATE booking SET status = 'completed' WHERE id_booking = ?";
        String getDetails = "SELECT id_aset FROM booking_detail WHERE id_booking = ?";
        String updateAsset = "UPDATE aset SET status_tersedia = 1 WHERE id_aset = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Integer> expiredIds = new ArrayList<>();
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(selectExpired)) {
                while (rs.next()) {
                    expiredIds.add(rs.getInt("id_booking"));
                }
            }

            if (expiredIds.isEmpty())
                return;

            conn.setAutoCommit(false);
            try (PreparedStatement pstBooking = conn.prepareStatement(updateBooking);
                    PreparedStatement pstDetails = conn.prepareStatement(getDetails);
                    PreparedStatement pstAsset = conn.prepareStatement(updateAsset)) {

                for (int idBooking : expiredIds) {
                    // Update Booking Status
                    pstBooking.setInt(1, idBooking);
                    pstBooking.executeUpdate();

                    // Get Assets and make them available
                    pstDetails.setInt(1, idBooking);
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
                System.out.println("Auto-completed " + expiredIds.size() + " bookings.");
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
}
