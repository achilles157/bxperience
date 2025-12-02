package tampilan.laporan;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.toedter.calendar.JDateChooser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.Vector;
import java.util.Locale;
import java.util.List;
import tampilan.components.RoundedPanel;
import tampilan.util.UIStyle;
import service.LaporanDAO;

public class LaporanPelangganPanel extends JPanel {

    private JDateChooser dateFrom, dateTo;
    private JComboBox<String> filterUrutkan;
    private JButton generateButton;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLayeredPane layeredPane;
    private JLabel loadingLabel;
    private NumberFormat currencyFormat;

    public LaporanPelangganPanel() {
        // Inisialisasi format mata uang
        currencyFormat = NumberFormat
                .getCurrencyInstance(new Locale.Builder().setLanguage("id").setRegion("ID").build());

        setLayout(new BorderLayout(20, 20));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Header Panel
        RoundedPanel headerPanel = new RoundedPanel(20, false);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(UIStyle.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel titleLabel = new JLabel("LAPORAN AKTIVITAS PELANGGAN", SwingConstants.CENTER);
        titleLabel.setFont(UIStyle.fontBold(24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // 2. Control Panel (Filter)
        RoundedPanel controlPanel = new RoundedPanel(10, true);
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        controlPanel.setBackground(UIStyle.CARD_BG);
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5); // Jarak antar komponen
        gbc.anchor = GridBagConstraints.WEST; // Rata kiri

        controlPanel.add(new JLabel("Dari Tanggal:"));
        dateFrom = new JDateChooser();
        UIStyle.styleDateChooser(dateFrom);
        dateFrom.setDate(new Date()); // Default hari ini
        controlPanel.add(dateFrom);

        controlPanel.add(new JLabel("Sampai Tanggal:"));
        dateTo = new JDateChooser();
        UIStyle.styleDateChooser(dateTo);
        dateTo.setDate(new Date()); // Default hari ini
        controlPanel.add(dateTo);

        controlPanel.add(new JLabel("Urutkan:"));
        filterUrutkan = new JComboBox<>(new String[] { "Total Pengeluaran", "Jumlah Transaksi" });
        UIStyle.styleComboBox(filterUrutkan);
        controlPanel.add(filterUrutkan);

        generateButton = UIStyle.modernButton("Tampilkan Laporan");
        generateButton.setBackground(UIStyle.SUCCESS_COLOR);
        generateButton.addActionListener(e -> loadReportData());
        controlPanel.add(generateButton);

        // 3. Report Panel (Tabel)
        JPanel reportPanel = new JPanel(new BorderLayout(10, 10));
        reportPanel.setOpaque(false);

        String[] columns = { "Peringkat", "Nama Pelanggan", "Instagram", "Jumlah Transaksi", "Total Pengeluaran" };
        tableModel = new DefaultTableModel(columns, 0);
        reportTable = new JTable(tableModel);
        UIStyle.styleTable(reportTable);
        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.getVerticalScrollBar().setUI(new UIStyle.ModernScrollBarUI());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        loadingLabel = new JLabel("Memuat data...", SwingConstants.CENTER);
        loadingLabel.setFont(UIStyle.fontBold(18));
        loadingLabel.setForeground(UIStyle.PRIMARY);
        loadingLabel.setOpaque(true);
        loadingLabel.setBackground(new Color(255, 255, 255, 200));
        loadingLabel.setVisible(false);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new GridBagLayout());
        GridBagConstraints gbcLayer = new GridBagConstraints();
        gbcLayer.gridx = 0;
        gbcLayer.gridy = 0;
        gbcLayer.weightx = 1.0;
        gbcLayer.weighty = 1.0;
        gbcLayer.fill = GridBagConstraints.BOTH;

        layeredPane.add(scrollPane, gbcLayer, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(loadingLabel, gbcLayer, JLayeredPane.PALETTE_LAYER);

        reportPanel.add(layeredPane, BorderLayout.CENTER);

        // Button Panel (Footer)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        JButton downloadButton = UIStyle.modernButton("Download PDF");
        downloadButton.setBackground(UIStyle.PRIMARY);
        downloadButton.setForeground(Color.WHITE);
        downloadButton.addActionListener(e -> downloadPDF());
        buttonPanel.add(downloadButton);
        reportPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 4. Content Wrapper
        JPanel contentWrapper = new JPanel(new BorderLayout(0, 20));
        contentWrapper.setOpaque(false);
        contentWrapper.add(controlPanel, BorderLayout.NORTH);
        contentWrapper.add(reportPanel, BorderLayout.CENTER);

        // 5. Menambahkan panel ke layout utama
        add(headerPanel, BorderLayout.NORTH);
        add(contentWrapper, BorderLayout.CENTER);
    }

    private void loadReportData() {
        if (dateFrom.getDate() == null || dateTo.getDate() == null) {
            UIStyle.showErrorMessage(this, "Silakan pilih rentang tanggal terlebih dahulu.");
            return;
        }

        setLoading(true);
        tableModel.setRowCount(0);

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                LaporanDAO dao = new LaporanDAO();
                String sortBy = filterUrutkan.getSelectedItem().toString();
                List<Object[]> data = dao.getPelangganAktif(dateFrom.getDate(), dateTo.getDate(), sortBy);

                DefaultTableModel tempModel = new DefaultTableModel(getTableColumns(), 0);
                for (Object[] row : data) {
                    // row: peringkat, nama, instagram, total_transaksi, total_belanja
                    tempModel.addRow(new Object[] {
                            row[0],
                            row[1],
                            row[2],
                            row[3],
                            currencyFormat.format(row[4])
                    });
                }
                return tempModel;
            }

            @Override
            protected void done() {
                try {
                    DefaultTableModel resultModel = get();
                    tableModel.setDataVector(resultModel.getDataVector(), getTableColumns());

                    // Set ulang lebar kolom
                    reportTable.getColumnModel().getColumn(0).setPreferredWidth(50); // Peringkat
                    reportTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Nama
                    reportTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Instagram
                    reportTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Jumlah
                    reportTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Total

                } catch (Exception e) {
                    e.printStackTrace();
                    UIStyle.showErrorMessage(LaporanPelangganPanel.this,
                            "Gagal memuat data laporan: " + e.getMessage());
                } finally {
                    setLoading(false);
                }
            }
        }.execute();
    }

    private Vector<String> getTableColumns() {
        Vector<String> columns = new Vector<>();
        columns.add("Peringkat");
        columns.add("Nama Pelanggan");
        columns.add("Instagram");
        columns.add("Jumlah Transaksi");
        columns.add("Total Pengeluaran");
        return columns;
    }

    private void setLoading(boolean isLoading) {
        loadingLabel.setVisible(isLoading);
        generateButton.setEnabled(!isLoading);
        reportTable.setEnabled(!isLoading);
    }

    private void downloadPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan PDF");
        fileChooser.setSelectedFile(
                new File(
                        "Laporan_Aktivitas_Pelanggan_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getParentFile(), file.getName() + ".pdf");
            }

            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Add Logo
                try {
                    URL logoUrl = getClass().getResource("/img/iconloginn.png");
                    if (logoUrl != null) {
                        Image logo = Image.getInstance(logoUrl);
                        logo.scaleToFit(100, 100);
                        logo.setAlignment(Element.ALIGN_CENTER);
                        document.add(logo);
                    }
                } catch (Exception e) {
                    System.err.println("Gagal memuat logo: " + e.getMessage());
                }

                // Add Header (Kop Surat)
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                Font subHeaderFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

                Paragraph companyName = new Paragraph("Consolerent Indonesia", headerFont);
                companyName.setAlignment(Element.ALIGN_CENTER);
                document.add(companyName);

                Paragraph companyAddress = new Paragraph(
                        "Alamat Perusahaan: Jalan Contoh No. 123, Kota, Negara\nTelp: (021) 12345678 | Email: info@perusahaan.com",
                        subHeaderFont);
                companyAddress.setAlignment(Element.ALIGN_CENTER);
                companyAddress.setSpacingAfter(20);
                document.add(companyAddress);

                // Add Title
                Paragraph title = new Paragraph("Laporan Aktivitas Pelanggan",
                        new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Add Date
                Paragraph date = new Paragraph("Tanggal: " + new SimpleDateFormat("dd MMMM yyyy").format(new Date()));
                date.setAlignment(Element.ALIGN_RIGHT);
                date.setSpacingAfter(10);
                document.add(date);

                // Add Table
                PdfPTable pdfTable = new PdfPTable(5); // 5 Columns
                pdfTable.setWidthPercentage(100);
                pdfTable.setSpacingBefore(10f);
                pdfTable.setSpacingAfter(10f);

                // Table Header
                String[] headers = { "Peringkat", "Nama Pelanggan", "Instagram", "Jumlah Transaksi",
                        "Total Pengeluaran" };
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(
                            new Phrase(header, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(8);
                    pdfTable.addCell(cell);
                }

                // Table Data
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        PdfPCell cell = new PdfPCell(new Phrase(tableModel.getValueAt(i, j).toString()));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5);
                        pdfTable.addCell(cell);
                    }
                }

                document.add(pdfTable);

                // Signature Section
                PdfPTable signatureTable = new PdfPTable(1);
                signatureTable.setWidthPercentage(100);
                signatureTable.setSpacingBefore(30f);

                PdfPCell signatureCell = new PdfPCell();
                signatureCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                signatureCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

                Paragraph placeDate = new Paragraph(
                        "Jakarta, " + new SimpleDateFormat("dd MMMM yyyy",
                                new Locale.Builder().setLanguage("id").setRegion("ID").build()).format(new Date()),
                        subHeaderFont);
                placeDate.setAlignment(Element.ALIGN_RIGHT);
                signatureCell.addElement(placeDate);

                Paragraph role = new Paragraph("Mengetahui,", subHeaderFont);
                role.setAlignment(Element.ALIGN_RIGHT);
                signatureCell.addElement(role);

                Paragraph space = new Paragraph("\n\n\n\n", subHeaderFont);
                signatureCell.addElement(space);

                Paragraph name = new Paragraph("( Nama Direktur )", subHeaderFont);
                name.setAlignment(Element.ALIGN_RIGHT);
                signatureCell.addElement(name);

                signatureTable.addCell(signatureCell);
                document.add(signatureTable);

                document.close();

                JOptionPane.showMessageDialog(this, "Laporan berhasil disimpan di:\n" + file.getAbsolutePath(),
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);

            } catch (DocumentException | IOException e) {
                e.printStackTrace();
                UIStyle.showErrorMessage(this, "Gagal menyimpan laporan: " + e.getMessage());
            }
        }
    }
}