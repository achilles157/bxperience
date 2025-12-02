package tampilan.aset;

import service.AsetDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import tampilan.components.RoundedPanel;
import tampilan.util.UIStyle;

public class AsetControl extends JPanel {

    private JTextField idField, namaField, kodeField, jumlahBarangField, hargaPerMenitField, hargaPerHariField;
    private JComboBox<String> kategoriCombo;
    private JTextArea deskripsiArea;
    private JCheckBox tersediaCheckbox, disewakanCheckbox;
    private JButton tambahButton;
    private AsetDAO asetDAO;

    public AsetControl() {
        asetDAO = new AsetDAO();
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        RoundedPanel mainPanel = new RoundedPanel(20);
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBackground(UIStyle.CARD_BG);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = UIStyle.createTitleLabel("Tambah Aset Baru");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        idField = new JTextField();
        idField.setEditable(false);
        generateIdAsync();

        namaField = new JTextField();
        kodeField = new JTextField();
        kodeField.setEditable(false);

        kategoriCombo = new JComboBox<>(new String[] {
                "PLAYSTATION 3", "PLAYSTATION 4", "PLAYSTATION 5", "TV 29INCH", "TV 40INCH",
                "STICK PS4", "STICK PS5", "NINTENDO", "VIP ROOM"
        });

        generateKodeAsync(kategoriCombo.getSelectedItem().toString());

        kategoriCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String kategori = kategoriCombo.getSelectedItem().toString();
                generateKodeAsync(kategori);
            }
        });

        deskripsiArea = new JTextArea(3, 20);
        jumlahBarangField = new JTextField();
        hargaPerMenitField = new JTextField();
        hargaPerHariField = new JTextField();
        tersediaCheckbox = new JCheckBox("Tersedia");
        disewakanCheckbox = new JCheckBox("Disewakan");

        tambahButton = UIStyle.modernButton("Tambah Aset");
        tambahButton.setBackground(UIStyle.PRIMARY);

        UIStyle.styleTextField(idField);
        UIStyle.styleTextField(namaField);
        UIStyle.styleTextField(kodeField);
        UIStyle.styleComboBox(kategoriCombo);
        UIStyle.styleTextArea(deskripsiArea);
        UIStyle.styleTextField(jumlahBarangField);
        UIStyle.styleTextField(hargaPerMenitField);
        UIStyle.styleTextField(hargaPerHariField);
        UIStyle.styleCheckBox(tersediaCheckbox);
        UIStyle.styleCheckBox(disewakanCheckbox);

        addValidationListener(namaField);
        addValidationListener(jumlahBarangField);
        addValidationListener(hargaPerMenitField);
        addValidationListener(hargaPerHariField);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("ID Aset"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(createLabel("Nama Barang"), gbc);
        gbc.gridx = 1;
        formPanel.add(namaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(createLabel("Kode Barang"), gbc);
        gbc.gridx = 1;
        formPanel.add(kodeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(createLabel("Kategori"), gbc);
        gbc.gridx = 1;
        formPanel.add(kategoriCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(createLabel("Deskripsi"), gbc);
        gbc.gridx = 1;
        JScrollPane deskripsiScroll = new JScrollPane(deskripsiArea);
        deskripsiScroll.getVerticalScrollBar().setUI(new UIStyle.ModernScrollBarUI());
        formPanel.add(deskripsiScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(createLabel("Jumlah Barang"), gbc);
        gbc.gridx = 1;
        formPanel.add(jumlahBarangField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(createLabel("Harga per Menit"), gbc);
        gbc.gridx = 1;
        formPanel.add(hargaPerMenitField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(createLabel("Harga per Hari"), gbc);
        gbc.gridx = 1;
        formPanel.add(hargaPerHariField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(createLabel("Status"), gbc);
        gbc.gridx = 1;
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(tersediaCheckbox);
        statusPanel.add(disewakanCheckbox);
        formPanel.add(statusPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(tambahButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        formPanel.add(Box.createVerticalGlue(), gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIStyle.CARD_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar().setUI(new UIStyle.ModernScrollBarUI());

        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        disewakanCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean selected = disewakanCheckbox.isSelected();
                hargaPerHariField.setEnabled(selected);
                if (!selected) {
                    hargaPerHariField.setText("");

                    UIStyle.styleTextField(hargaPerHariField);
                } else {

                    if (hargaPerHariField.getText().trim().isEmpty()) {
                        hargaPerHariField.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
                    }
                }
            }
        });

        tambahButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (!validateInput()) {
                    UIStyle.showErrorMessage(AsetControl.this, "Mohon lengkapi semua kolom yang wajib diisi.");
                    return;
                }

                String namaBarang = namaField.getText();
                String kategori = kategoriCombo.getSelectedItem().toString();
                String deskripsi = deskripsiArea.getText();

                int jumlah;
                double hargaMenit;
                double hargaHari = 0.0;

                try {
                    jumlah = Integer.parseInt(jumlahBarangField.getText());
                    hargaMenit = Double.parseDouble(hargaPerMenitField.getText());
                    if (disewakanCheckbox.isSelected()) {
                        hargaHari = Double.parseDouble(hargaPerHariField.getText());
                    }
                } catch (NumberFormatException ex) {
                    UIStyle.showErrorMessage(AsetControl.this,
                            "Input jumlah atau harga harus berupa angka yang valid.");
                    return;
                }

                boolean isTersedia = tersediaCheckbox.isSelected();
                boolean isDisewakan = disewakanCheckbox.isSelected();

                final int finalJumlah = jumlah;
                final double finalHargaMenit = hargaMenit;
                final double finalHargaHari = hargaHari;

                tambahButton.setEnabled(false);
                tambahButton.setText("Menyimpan...");

                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        asetDAO.insertAset(namaBarang, kategori, deskripsi, finalJumlah, finalHargaMenit,
                                finalHargaHari, isTersedia,
                                isDisewakan);
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            UIStyle.showSuccessMessage(AsetControl.this, jumlah + " aset berhasil ditambahkan!");
                            clearForm();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            UIStyle.showErrorMessage(AsetControl.this, "Gagal menambahkan aset: " + ex.getMessage());
                        } finally {
                            tambahButton.setEnabled(true);
                            tambahButton.setText("Tambah Aset");
                        }
                    }
                }.execute();
            }

        });
    }

    private void generateIdAsync() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return asetDAO.generateNextIdAset();
            }

            @Override
            protected void done() {
                try {
                    idField.setText(get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void generateKodeAsync(String kategori) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return asetDAO.generateNextKodeBarang(kategori);
            }

            @Override
            protected void done() {
                try {
                    kodeField.setText(get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIStyle.fontMedium(14));
        label.setForeground(UIStyle.TEXT);
        return label;
    }

    private boolean validateInput() {
        boolean valid = true;
        if (namaField.getText().trim().isEmpty()) {
            namaField.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
            valid = false;
        } else {
            UIStyle.styleTextField(namaField);
        }

        if (jumlahBarangField.getText().trim().isEmpty()) {
            jumlahBarangField.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
            valid = false;
        } else {
            UIStyle.styleTextField(jumlahBarangField);
        }

        if (hargaPerMenitField.getText().trim().isEmpty()) {
            hargaPerMenitField.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
            valid = false;
        } else {
            UIStyle.styleTextField(hargaPerMenitField);
        }

        if (disewakanCheckbox.isSelected() && hargaPerHariField.getText().trim().isEmpty()) {
            hargaPerHariField.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
            valid = false;
        } else {
            UIStyle.styleTextField(hargaPerHariField);
        }

        return valid;
    }

    private void clearForm() {
        generateIdAsync();
        namaField.setText("");
        generateKodeAsync(kategoriCombo.getSelectedItem().toString());
        kategoriCombo.setSelectedIndex(0);
        deskripsiArea.setText("");
        jumlahBarangField.setText("");
        hargaPerMenitField.setText("");
        hargaPerHariField.setText("");
        tersediaCheckbox.setSelected(false);
        disewakanCheckbox.setSelected(false);
        hargaPerHariField.setEnabled(false);
    }

    private void addValidationListener(JTextField field) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setBorder(BorderFactory.createLineBorder(UIStyle.DANGER_COLOR, 1, true));
                } else {

                    UIStyle.styleTextField(field);
                }
            }
        });
    }
}