package tampilan.booking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tampilan.util.UIStyle;
import tampilan.components.*;
import tampilan.booking.components.*;
import service.BookingDAO;

/**
 * Panel untuk menangani proses booking manual.
 * Memungkinkan pengguna untuk memilih area, kategori, waktu, dan durasi sewa,
 * serta menghitung total harga secara otomatis.
 */
public class BookingManual extends JPanel {
    private BookingFormPanel formPanel;
    private RoundedTextField priceField, diskonField, totalField;
    private BookingDAO bookingDAO;

    /**
     * Konstruktor untuk inisialisasi panel booking manual.
     * Mengatur layout, scroll pane, dan komponen UI utama.
     */
    public BookingManual() {
        bookingDAO = new BookingDAO();
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getVerticalScrollBar().setUI(new UIStyle.ModernScrollBarUI());
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(UIStyle.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        scrollPane.setViewportView(contentPanel);
        add(scrollPane, BorderLayout.CENTER);

        initComponents(contentPanel);
    }

    /**
     * Menginisialisasi komponen-komponen UI pada panel konten.
     * Termasuk header, form panel, payment panel, dan tombol submit.
     *
     * @param contentPanel Panel tempat komponen akan ditambahkan.
     */
    private void initComponents(JPanel contentPanel) {
        // Header
        RoundedPanel headerPanel = new RoundedPanel(20, false);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(UIStyle.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));

        JLabel headerLabel = new JLabel("BOOKING CONSOLE", SwingConstants.CENTER);
        headerLabel.setFont(UIStyle.fontBold(28));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(new EmptyBorder(18, 10, 18, 10));
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Main Wrapper
        JPanel mainWrapper = new JPanel();
        mainWrapper.setLayout(new BoxLayout(mainWrapper, BoxLayout.Y_AXIS));
        mainWrapper.setOpaque(false);

        // 1. Form Panel
        formPanel = new BookingFormPanel();
        mainWrapper.add(formPanel);
        mainWrapper.add(Box.createVerticalStrut(20));

        // 2. Payment Panel
        RoundedPanel paymentPanel = new RoundedPanel(25);
        paymentPanel.setLayout(new GridBagLayout());
        paymentPanel.setBackground(UIStyle.CARD_BG);
        initPaymentPanel(paymentPanel);
        mainWrapper.add(paymentPanel);

        contentPanel.add(mainWrapper, BorderLayout.CENTER);

        // Submit Button
        RoundedButton submitButton = new RoundedButton("Konfirmasi Booking", 10);
        submitButton.setBackground(UIStyle.SUCCESS_COLOR);
        submitButton.setPreferredSize(new Dimension(200, 50));
        submitButton.addActionListener(e -> submitBooking());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Logic Wiring
        setupListeners();
    }

    /**
     * Menginisialisasi panel informasi pembayaran.
     * Menampilkan field untuk harga per menit dan total harga.
     *
     * @param panel Panel pembayaran yang akan diinisialisasi.
     */
    private void initPaymentPanel(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        JLabel title = new JLabel("Informasi Pembayaran");
        title.setFont(UIStyle.fontBold(18));
        title.setForeground(UIStyle.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);
        gbc.gridwidth = 1;

        priceField = new RoundedTextField();
        priceField.setEditable(false);
        diskonField = new RoundedTextField();
        totalField = new RoundedTextField();
        totalField.setEditable(false);

        addField(panel, "Harga per Menit:", priceField, gbc, 1);
        addField(panel, "Diskon (%):", diskonField, gbc, 2);
        addField(panel, "Total Harga:", totalField, gbc, 3);
    }

    /**
     * Menambahkan field label dan komponen input ke dalam panel dengan layout
     * GridBagLayout.
     *
     * @param panel     Panel tujuan.
     * @param labelText Teks label.
     * @param field     Komponen input (misalnya JTextField).
     * @param gbc       GridBagConstraints untuk layout.
     * @param row       Baris tempat komponen akan ditempatkan.
     */
    private void addField(JPanel panel, String labelText, Component field, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setFont(UIStyle.fontMedium(16));
        label.setForeground(UIStyle.TEXT);
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(label, gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    /**
     * Mengatur listener untuk interaksi pengguna pada form.
     * Menangani perubahan area, tanggal, waktu, durasi, kategori, dan jumlah unit
     * untuk memperbarui ketersediaan dan harga secara dinamis.
     */
    private void setupListeners() {
        // Load initial categories based on default area
        loadCategoriesByArea();
        updateConsoleVisibility();

        // Update categories when area changes
        formPanel.addAreaListener(e -> {
            loadCategoriesByArea();
            updateConsoleVisibility();
            updateAvailableExperiences();
            calculatePrice();
        });

        // Update availability when date/time/duration changes
        formPanel.addAvailabilityListener(evt -> updateAvailableExperiences());
        formPanel.addJamListener(e -> updateAvailableExperiences());

        // Listeners for price calculation
        formPanel.addDurasiListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateAvailableExperiences();
                calculatePrice();
            }

            public void insertUpdate(DocumentEvent e) {
                updateAvailableExperiences();
                calculatePrice();
            }

            public void removeUpdate(DocumentEvent e) {
                updateAvailableExperiences();
                calculatePrice();
            }
        });

        formPanel.addKategoriListener(e -> calculatePrice());

        formPanel.addJumlahListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                calculatePrice();
            }

            public void insertUpdate(DocumentEvent e) {
                calculatePrice();
            }

            public void removeUpdate(DocumentEvent e) {
                calculatePrice();
            }
        });

        formPanel.addConsoleListener(e -> calculatePrice());

        diskonField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                calculatePrice();
            }

            public void insertUpdate(DocumentEvent e) {
                calculatePrice();
            }

            public void removeUpdate(DocumentEvent e) {
                calculatePrice();
            }
        });
    }

    /**
     * Memperbarui visibilitas opsi konsol tambahan berdasarkan area yang dipilih.
     * Opsi konsol hanya muncul jika area yang dipilih adalah "VIP Room".
     */
    private void updateConsoleVisibility() {
        String area = formPanel.getArea();
        boolean isVIP = "VIP Room".equalsIgnoreCase(area);
        formPanel.setConsoleVisibility(isVIP);
    }

    /**
     * Memuat daftar kategori aset berdasarkan area yang dipilih.
     * Menggunakan SwingWorker untuk mengambil data dari database secara asinkron.
     */
    private void loadCategoriesByArea() {
        String area = formPanel.getArea();
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return bookingDAO.getAllCategoriesByArea(area);
            }

            @Override
            protected void done() {
                try {
                    List<String> categories = get();
                    formPanel.setKategoriItems(categories);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Memperbarui daftar pengalaman (kategori) yang tersedia berdasarkan tanggal,
     * waktu, dan durasi yang dipilih.
     * Melakukan pengecekan ketersediaan ke database dan menampilkan pesan jika
     * tidak ada unit yang tersedia.
     */
    private void updateAvailableExperiences() {
        // Only check availability if all fields are filled
        if (formPanel.getTanggal() == null || formPanel.getJam() == null || formPanel.getDurasi().isEmpty()) {
            // If fields are incomplete, just reload categories for the selected area
            loadCategoriesByArea();
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(formPanel.getDurasi());
        } catch (NumberFormatException e) {
            return;
        }

        java.sql.Date date = new java.sql.Date(formPanel.getTanggal().getTime());
        String time = formPanel.getJam();
        String area = formPanel.getArea();

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return bookingDAO.getAvailableExperiencesByArea(date, time, duration, area);
            }

            @Override
            protected void done() {
                try {
                    List<String> categories = get();
                    formPanel.setKategoriItems(categories);
                    if (categories.isEmpty()) {
                        JOptionPane.showMessageDialog(BookingManual.this,
                                "Tidak ada Console tersedia pada jam tersebut di area " + area + ".");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Menghitung estimasi total harga booking.
     * Mengambil harga per menit dari database dan mengalikannya dengan durasi dan
     * jumlah unit.
     * Menambahkan biaya tambahan jika kedua konsol (PS5 & Nintendo) dipilih di VIP
     * Room.
     */
    private void calculatePrice() {
        String category = formPanel.getKategori();
        String durationStr = formPanel.getDurasi();
        String quantityStr = formPanel.getJumlah();

        if (category == null || durationStr.isEmpty() || quantityStr.isEmpty()) {
            priceField.setText("");
            totalField.setText("");
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            int quantity = Integer.parseInt(quantityStr);

            new SwingWorker<Double, Void>() {
                @Override
                protected Double doInBackground() throws Exception {
                    return bookingDAO.getPricePerMinute(category);
                }

                @Override
                protected void done() {
                    try {
                        double pricePerMinute = get();
                        double total = Math.ceil(pricePerMinute * duration * quantity);

                        if (formPanel.isPS5Selected() && formPanel.isNintendoSelected()) {
                            total += (15000 * quantity);
                        }

                        double diskonPersen = 0;
                        try {
                            if (!diskonField.getText().isEmpty())
                                diskonPersen = Double.parseDouble(diskonField.getText());
                        } catch (NumberFormatException e) {
                        }

                        // Validate percentage
                        if (diskonPersen < 0)
                            diskonPersen = 0;
                        if (diskonPersen > 100)
                            diskonPersen = 100;

                        double diskonNominal = total * (diskonPersen / 100.0);

                        priceField.setText(UIStyle.formatCurrency(pricePerMinute));
                        totalField.setText(UIStyle.formatCurrency(total - diskonNominal));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute();

        } catch (NumberFormatException e) {
            // Ignore invalid number format
        }
    }

    /**
     * Memproses pengajuan booking.
     * Memvalidasi input pengguna dan menyimpan data booking ke database.
     * Menampilkan pesan sukses atau error berdasarkan hasil operasi.
     */
    private void submitBooking() {
        if (formPanel.getNama().isEmpty() || formPanel.getTanggal() == null || formPanel.getDurasi().isEmpty() ||
                formPanel.getKategori() == null || formPanel.getJumlah().isEmpty()) {
            UIStyle.showErrorMessage(this, "Harap lengkapi semua data.");
            return;
        }

        // Validate VIP console selection
        if ("VIP Room".equalsIgnoreCase(formPanel.getArea())) {
            if (!formPanel.isPS5Selected() && !formPanel.isNintendoSelected()) {
                UIStyle.showErrorMessage(this, "Harap pilih setidaknya satu console untuk VIP Room.");
                return;
            }
        }

        try {
            String nama = formPanel.getNama();
            String noHp = formPanel.getNoHp();
            java.sql.Date date = new java.sql.Date(formPanel.getTanggal().getTime());
            String time = formPanel.getJam();
            int duration = Integer.parseInt(formPanel.getDurasi());
            String category = formPanel.getKategori();
            int quantity = Integer.parseInt(formPanel.getJumlah());
            boolean extraConsole = formPanel.isPS5Selected() && formPanel.isNintendoSelected();

            double diskonPersenValue = 0;
            try {
                if (!diskonField.getText().isEmpty())
                    diskonPersenValue = Double.parseDouble(diskonField.getText());
            } catch (NumberFormatException e) {
            }
            if (diskonPersenValue < 0)
                diskonPersenValue = 0;
            if (diskonPersenValue > 100)
                diskonPersenValue = 100;

            final double finalDiskonPersen = diskonPersenValue;

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // 1. Get Price
                    double pricePerMinute = bookingDAO.getPricePerMinute(category);
                    double total = Math.ceil(pricePerMinute * duration * quantity);
                    if (extraConsole) {
                        total += (15000 * quantity);
                    }

                    // 2. Calculate Discount Nominal
                    double diskonNominal = total * (finalDiskonPersen / 100.0);

                    // 3. Create Booking with Nominal Discount
                    return bookingDAO.createBooking(nama, noHp, date, time, duration,
                            category, quantity, extraConsole, diskonNominal);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            UIStyle.showSuccessMessage(BookingManual.this, "Booking berhasil dibuat!");
                            formPanel.reset();
                            priceField.setText("");
                            diskonField.setText("");
                            totalField.setText("");
                            loadCategoriesByArea(); // Reload categories after booking
                        } else {
                            UIStyle.showErrorMessage(BookingManual.this, "Gagal membuat booking. Silakan coba lagi.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        UIStyle.showErrorMessage(BookingManual.this, "Error: " + e.getMessage());
                    }
                }
            }.execute();

        } catch (NumberFormatException e) {
            UIStyle.showErrorMessage(this, "Format angka tidak valid.");
        }
    }
}