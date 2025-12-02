package tampilan.playathome;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

import tampilan.util.UIStyle;
import tampilan.components.*;
import tampilan.playathome.components.*;
import service.PlayAtHomeDAO;

public class PlayAtHomeManual extends JPanel {
    private PlayAtHomeFormPanel formPanel;
    private PlayAtHomeItemPanel itemPanel;
    private RoundedTextField hargaField, ongkirField, totalField;
    private PlayAtHomeDAO playAtHomeDAO;

    public PlayAtHomeManual() {
        playAtHomeDAO = new PlayAtHomeDAO();
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

    private void initComponents(JPanel contentPanel) {

        RoundedPanel headerPanel = new RoundedPanel(20, false);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(UIStyle.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));

        JLabel headerLabel = new JLabel("PLAY AT HOME", SwingConstants.CENTER);
        headerLabel.setFont(UIStyle.fontBold(28));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(new EmptyBorder(20, 10, 20, 10));
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel mainWrapper = new JPanel();
        mainWrapper.setLayout(new BoxLayout(mainWrapper, BoxLayout.Y_AXIS));
        mainWrapper.setOpaque(false);

        formPanel = new PlayAtHomeFormPanel();
        mainWrapper.add(formPanel);
        mainWrapper.add(Box.createVerticalStrut(20));

        itemPanel = new PlayAtHomeItemPanel(playAtHomeDAO);
        mainWrapper.add(itemPanel);
        mainWrapper.add(Box.createVerticalStrut(20));

        RoundedPanel paymentPanel = new RoundedPanel(25);
        paymentPanel.setLayout(new GridBagLayout());
        paymentPanel.setBackground(UIStyle.CARD_BG);
        initPaymentPanel(paymentPanel);
        mainWrapper.add(paymentPanel);

        contentPanel.add(mainWrapper, BorderLayout.CENTER);

        RoundedButton submitButton = new RoundedButton("Simpan Transaksi", 10);
        submitButton.setBackground(UIStyle.SUCCESS_COLOR);
        submitButton.setPreferredSize(new Dimension(200, 50));
        submitButton.addActionListener(e -> submitTransaction());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        formPanel.addDateChangeListener(evt -> calculateTotal());
        itemPanel.setOnTableChangeListener(this::calculateTotal);

        ongkirField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                calculateTotal();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                calculateTotal();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                calculateTotal();
            }
        });
    }

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

        hargaField = new RoundedTextField();
        hargaField.setEditable(false);
        ongkirField = new RoundedTextField();
        totalField = new RoundedTextField();
        totalField.setEditable(false);

        addField(panel, "Harga Sewa:", hargaField, gbc, 1);
        addField(panel, "Ongkir:", ongkirField, gbc, 2);
        addField(panel, "Total:", totalField, gbc, 3);
    }

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

    private void calculateTotal() {
        if (formPanel.getDariDate() == null || formPanel.getSampaiDate() == null)
            return;

        long diff = formPanel.getSampaiDate().getTime() - formPanel.getDariDate().getTime();
        int days = (int) (diff / (1000 * 60 * 60 * 24)) + 1;

        if (days <= 0)
            return;

        itemPanel.updatePrices(days);
        double itemTotal = itemPanel.getTotalPrice();
        hargaField.setText(String.valueOf(itemTotal));

        double ongkir = 0;
        try {
            if (!ongkirField.getText().isEmpty())
                ongkir = Double.parseDouble(ongkirField.getText());
        } catch (NumberFormatException e) {
        }

        totalField.setText(String.valueOf(itemTotal + ongkir));
    }

    private void submitTransaction() {
        if (formPanel.getNama().isEmpty() || formPanel.getLokasi().isEmpty() ||
                formPanel.getDariDate() == null || formPanel.getSampaiDate() == null ||
                itemPanel.getItems().isEmpty()) {
            UIStyle.showErrorMessage(this, "Harap lengkapi semua data.");
            return;
        }

        List<PlayAtHomeDAO.RentalItem> items = itemPanel.getItems();
        String nama = formPanel.getNama();
        String lokasi = formPanel.getLokasi();
        String instagram = formPanel.getInstagram();
        java.sql.Date tglMulai = new java.sql.Date(formPanel.getDariDate().getTime());
        java.sql.Date tglSelesai = new java.sql.Date(formPanel.getSampaiDate().getTime());
        String metode = formPanel.getMetode();
        String alamatAntar = formPanel.getAlamatAntar();
        String alamatKembali = formPanel.getAlamatKembali();
        String keperluan = formPanel.getKeperluan();

        double ongkirValue = 0;
        try {
            if (!ongkirField.getText().isEmpty())
                ongkirValue = Double.parseDouble(ongkirField.getText());
        } catch (NumberFormatException e) {
        }
        final double finalOngkir = ongkirValue;
        final double finalTotal = Double.parseDouble(totalField.getText());

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return playAtHomeDAO.createRental(nama, lokasi, instagram, tglMulai, tglSelesai,
                        metode, alamatAntar, alamatKembali, keperluan, finalOngkir, finalTotal, items);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        UIStyle.showSuccessMessage(PlayAtHomeManual.this, "Transaksi berhasil disimpan.");
                        formPanel.reset();
                        itemPanel.reset();
                        hargaField.setText("");
                        ongkirField.setText("");
                        totalField.setText("");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    UIStyle.showErrorMessage(PlayAtHomeManual.this, "Gagal menyimpan: " + e.getMessage());
                }
            }
        }.execute();
    }
}