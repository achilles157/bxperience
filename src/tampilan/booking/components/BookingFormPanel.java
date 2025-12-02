package tampilan.booking.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import com.toedter.calendar.JDateChooser;
import tampilan.components.*;
import tampilan.util.UIStyle;

public class BookingFormPanel extends RoundedPanel {
    private RoundedTextField namaField, noHpField;
    private JDateChooser tanggalDate;
    private RoundedComboBox<String> jamCombo;
    private RoundedTextField durasiField;
    private RoundedComboBox<String> areaCombo;
    private RoundedComboBox<String> kategoriCombo;
    private RoundedTextField jumlahField;

    private JCheckBox ps5Check, nintendoCheck;
    private JPanel consolePanel;

    public BookingFormPanel() {
        super(25);
        setLayout(new GridBagLayout());
        setBackground(UIStyle.CARD_BG);
        initForm();
    }

    private void initForm() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        int row = 0;

        // Section: Data Diri
        addSectionTitle("Data Diri", gbc, row++);

        namaField = new RoundedTextField();
        noHpField = new RoundedTextField();

        addField("Nama:", namaField, gbc, row++);
        addField("No. HP:", noHpField, gbc, row++);

        // Section: Detail Booking
        addSectionTitle("Detail Booking", gbc, row++);

        // Area Selection
        areaCombo = new RoundedComboBox<>(new String[] { "Regular", "VIP Room" });
        addField("Area:", areaCombo, gbc, row++);

        // Console Selection (Hidden by default)
        consolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        consolePanel.setOpaque(false);
        ps5Check = new JCheckBox("PS5");
        nintendoCheck = new JCheckBox("Nintendo Switch");
        UIStyle.styleCheckBox(ps5Check);
        UIStyle.styleCheckBox(nintendoCheck);
        consolePanel.add(ps5Check);
        consolePanel.add(Box.createHorizontalStrut(15));
        consolePanel.add(nintendoCheck);
        consolePanel.setVisible(false); // Hidden initially

        addField("Console:", consolePanel, gbc, row++);

        tanggalDate = new JDateChooser();
        UIStyle.styleDateChooser(tanggalDate);

        String[] jam = new String[15];
        for (int i = 0; i < 15; i++) {
            jam[i] = String.format("%02d:00:00", i + 9); // 09:00 to 23:00
        }
        jamCombo = new RoundedComboBox<>(jam);

        durasiField = new RoundedTextField();

        // Kategori will be populated dynamically based on Area
        kategoriCombo = new RoundedComboBox<>();

        jumlahField = new RoundedTextField();

        addField("Tanggal:", tanggalDate, gbc, row++);
        addField("Jam Mulai:", jamCombo, gbc, row++);
        addField("Durasi (Menit):", durasiField, gbc, row++);
        addField("Experience:", kategoriCombo, gbc, row++);
        addField("Jumlah Unit:", jumlahField, gbc, row++);
    }

    private void addSectionTitle(String title, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(title);
        label.setFont(UIStyle.fontBold(18));
        label.setForeground(UIStyle.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        add(label, gbc);
        gbc.gridwidth = 1;
    }

    private void addField(String labelText, Component field, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setFont(UIStyle.fontMedium(16));
        label.setForeground(UIStyle.TEXT);
        gbc.gridx = 0;
        gbc.gridy = row;
        add(label, gbc);
        gbc.gridx = 1;
        add(field, gbc);
    }

    // Getters
    public String getNama() {
        return namaField.getText();
    }

    public String getNoHp() {
        return noHpField.getText();
    }

    public String getArea() {
        return (String) areaCombo.getSelectedItem();
    }

    public boolean isPS5Selected() {
        return ps5Check.isSelected();
    }

    public boolean isNintendoSelected() {
        return nintendoCheck.isSelected();
    }

    public void setConsoleVisibility(boolean visible) {
        consolePanel.setVisible(visible);
        // If hidden, uncheck both to avoid accidental charges
        if (!visible) {
            ps5Check.setSelected(false);
            nintendoCheck.setSelected(false);
        }
    }

    public java.util.Date getTanggal() {
        return tanggalDate.getDate();
    }

    public String getJam() {
        return (String) jamCombo.getSelectedItem();
    }

    public String getDurasi() {
        return durasiField.getText();
    }

    public String getKategori() {
        return (String) kategoriCombo.getSelectedItem();
    }

    public String getJumlah() {
        return jumlahField.getText();
    }

    public void setKategoriItems(java.util.List<String> items) {
        kategoriCombo.removeAllItems();
        for (String item : items)
            kategoriCombo.addItem(item);
    }

    public void addAvailabilityListener(java.beans.PropertyChangeListener listener) {
        tanggalDate.addPropertyChangeListener("date", listener);
    }

    public void addJamListener(ActionListener listener) {
        jamCombo.addActionListener(listener);
    }

    public void addAreaListener(ActionListener listener) {
        areaCombo.addActionListener(listener);
    }

    public void addKategoriListener(ActionListener listener) {
        kategoriCombo.addActionListener(listener);
    }

    public void addDurasiListener(javax.swing.event.DocumentListener listener) {
        durasiField.getDocument().addDocumentListener(listener);
    }

    public void addJumlahListener(javax.swing.event.DocumentListener listener) {
        jumlahField.getDocument().addDocumentListener(listener);
    }

    public void addConsoleListener(ActionListener listener) {
        ps5Check.addActionListener(listener);
        nintendoCheck.addActionListener(listener);
    }

    public void reset() {
        namaField.setText("");
        noHpField.setText("");
        areaCombo.setSelectedIndex(0);
        ps5Check.setSelected(false);
        nintendoCheck.setSelected(false);
        consolePanel.setVisible(false);
        tanggalDate.setDate(null);
        jamCombo.setSelectedIndex(0);
        durasiField.setText("");
        jumlahField.setText("");
    }
}
