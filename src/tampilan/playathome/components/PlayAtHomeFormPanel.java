package tampilan.playathome.components;

import javax.swing.*;
import java.awt.*;
import com.toedter.calendar.JDateChooser;
import tampilan.components.*;
import tampilan.util.UIStyle;

public class PlayAtHomeFormPanel extends RoundedPanel {
    private RoundedTextField lokasiField, namaField, instagramField;
    private JDateChooser dariDate, sampaiDate;
    private RoundedComboBox<String> metodeCombo;
    private RoundedTextField alamatAntarField, alamatKembaliField, keperluanField;

    public PlayAtHomeFormPanel() {
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

        addSectionTitle("Informasi Penyewa", gbc, row++);

        lokasiField = new RoundedTextField();
        namaField = new RoundedTextField();
        instagramField = new RoundedTextField();

        addField("Lokasi:", lokasiField, gbc, row++);
        addField("Nama Penyewa:", namaField, gbc, row++);
        addField("Akun Instagram:", instagramField, gbc, row++);

        addSectionTitle("Detail Penyewaan", gbc, row++);

        dariDate = new JDateChooser();
        UIStyle.styleDateChooser(dariDate);
        sampaiDate = new JDateChooser();
        UIStyle.styleDateChooser(sampaiDate);

        metodeCombo = new RoundedComboBox<>(new String[] { "Pick Up", "Delivery" });
        alamatAntarField = new RoundedTextField();
        alamatKembaliField = new RoundedTextField();
        keperluanField = new RoundedTextField();

        addField("Tanggal Mulai:", dariDate, gbc, row++);
        addField("Tanggal Selesai:", sampaiDate, gbc, row++);
        addField("Metode Pengambilan:", metodeCombo, gbc, row++);
        addField("Alamat Antar:", alamatAntarField, gbc, row++);
        addField("Alamat Kembali:", alamatKembaliField, gbc, row++);
        addField("Keperluan:", keperluanField, gbc, row++);
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

    public String getLokasi() {
        return lokasiField.getText();
    }

    public String getNama() {
        return namaField.getText();
    }

    public String getInstagram() {
        return instagramField.getText();
    }

    public java.util.Date getDariDate() {
        return dariDate.getDate();
    }

    public java.util.Date getSampaiDate() {
        return sampaiDate.getDate();
    }

    public String getMetode() {
        return (String) metodeCombo.getSelectedItem();
    }

    public String getAlamatAntar() {
        return alamatAntarField.getText();
    }

    public String getAlamatKembali() {
        return alamatKembaliField.getText();
    }

    public String getKeperluan() {
        return keperluanField.getText();
    }

    public void addDateChangeListener(java.beans.PropertyChangeListener listener) {
        dariDate.addPropertyChangeListener("date", listener);
        sampaiDate.addPropertyChangeListener("date", listener);
    }

    public void reset() {
        lokasiField.setText("");
        namaField.setText("");
        instagramField.setText("");
        dariDate.setDate(null);
        sampaiDate.setDate(null);
        metodeCombo.setSelectedIndex(0);
        alamatAntarField.setText("");
        alamatKembaliField.setText("");
        keperluanField.setText("");
    }
}
