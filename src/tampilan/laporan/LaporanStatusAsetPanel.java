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
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import tampilan.components.RoundedPanel;
import tampilan.util.UIStyle;
import service.LaporanDAO;

public class LaporanStatusAsetPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JLabel totalAsetLabel, tersediaLabel, disewakanLabel;

    public LaporanStatusAsetPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIStyle.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        RoundedPanel headerPanel = new RoundedPanel(15);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(UIStyle.CARD_BG);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Laporan Status Aset");
        titleLabel.setFont(UIStyle.fontBold(24));
        titleLabel.setForeground(UIStyle.PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        add(headerPanel, BorderLayout.NORTH);

        // Summary Cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setOpaque(false);

        totalAsetLabel = createSummaryCard("Total Aset", "0", UIStyle.PRIMARY);
        tersediaLabel = createSummaryCard("Tersedia", "0", UIStyle.SUCCESS_COLOR);
        disewakanLabel = createSummaryCard("Disewakan", "0", UIStyle.WARNING_COLOR);

        summaryPanel.add(totalAsetLabel.getParent());
        summaryPanel.add(tersediaLabel.getParent());
        summaryPanel.add(disewakanLabel.getParent());

        // Table
        model = new DefaultTableModel(new String[] { "Kategori", "Total", "Tersedia", "Disewakan" }, 0);
        table = new JTable(model);
        UIStyle.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getVerticalScrollBar().setUI(new UIStyle.ModernScrollBarUI());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        RoundedPanel contentPanel = new RoundedPanel(15);
        contentPanel.setLayout(new BorderLayout(0, 20));
        contentPanel.setBackground(UIStyle.CARD_BG);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        contentPanel.add(summaryPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton downloadButton = UIStyle.modernButton("Download PDF");
        downloadButton.setBackground(UIStyle.PRIMARY);
        downloadButton.setForeground(Color.WHITE);
        downloadButton.addActionListener(e -> downloadPDF());

        JButton refreshButton = UIStyle.modernButton("Refresh Data");
        refreshButton.addActionListener(e -> loadData());

        buttonPanel.add(downloadButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);

        loadData();
    }

    private JLabel createSummaryCard(String title, String value, Color color) {
        RoundedPanel card = new RoundedPanel(10);
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIStyle.fontMedium(14));
        titleLbl.setForeground(UIStyle.TEXT_LIGHT);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(UIStyle.fontBold(28));
        valueLbl.setForeground(color);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);

        return valueLbl;
    }

    private void loadData() {
        new SwingWorker<Void, Void>() {
            int total = 0, tersedia = 0, disewakan = 0;

            @Override
            protected Void doInBackground() throws Exception {
                model.setRowCount(0);
                LaporanDAO dao = new LaporanDAO();
                List<Object[]> data = dao.getStatusAset();

                for (Object[] row : data) {
                    String cat = (String) row[0];
                    int t = (int) row[1];
                    int av = (int) row[2];
                    int r = (int) row[3];

                    total += t;
                    tersedia += av;
                    disewakan += r;

                    model.addRow(new Object[] { cat, t, av, r });
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    totalAsetLabel.setText(String.valueOf(total));
                    tersediaLabel.setText(String.valueOf(tersedia));
                    disewakanLabel.setText(String.valueOf(disewakan));
                } catch (Exception e) {
                    e.printStackTrace();
                    UIStyle.showErrorMessage(LaporanStatusAsetPanel.this, "Gagal memuat data: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void downloadPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan PDF");
        fileChooser.setSelectedFile(
                new File("Laporan_Status_Aset_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".pdf"));

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
                Paragraph title = new Paragraph("Laporan Status Aset",
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
                PdfPTable pdfTable = new PdfPTable(4);
                pdfTable.setWidthPercentage(100);
                pdfTable.setSpacingBefore(10f);
                pdfTable.setSpacingAfter(10f);

                // Table Header
                String[] headers = { "Kategori", "Total Aset", "Tersedia", "Disewakan" };
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(
                            new Phrase(header, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(8);
                    pdfTable.addCell(cell);
                }

                // Table Data
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        PdfPCell cell = new PdfPCell(new Phrase(model.getValueAt(i, j).toString()));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(5);
                        pdfTable.addCell(cell);
                    }
                }

                document.add(pdfTable);

                // Summary
                document.add(new Paragraph("\nRingkasan:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
                document.add(new Paragraph("Total Aset: " + totalAsetLabel.getText()));
                document.add(new Paragraph("Aset Tersedia: " + tersediaLabel.getText()));
                document.add(new Paragraph("Aset Disewakan: " + disewakanLabel.getText()));

                // Signature Section
                PdfPTable signatureTable = new PdfPTable(1);
                signatureTable.setWidthPercentage(100);
                signatureTable.setSpacingBefore(30f);

                PdfPCell signatureCell = new PdfPCell();
                signatureCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                signatureCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

                Paragraph placeDate = new Paragraph("Jakarta, "
                        + new SimpleDateFormat("dd MMMM yyyy", java.util.Locale.forLanguageTag("id-ID"))
                                .format(new Date()),
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
