package tampilan.playathome.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import tampilan.components.*;
import tampilan.util.UIStyle;
import service.PlayAtHomeDAO;

public class PlayAtHomeItemPanel extends RoundedPanel {
    private RoundedComboBox<String> kategoriCombo;
    private RoundedComboBox<String> itemCombo;
    private RoundedTextField jumlahField;
    private JLabel countLabel;
    private JTable itemTable;
    private DefaultTableModel itemModel;
    private JScrollPane itemScroll;
    private PlayAtHomeDAO playAtHomeDAO;
    private Runnable onTableChange;

    public PlayAtHomeItemPanel(PlayAtHomeDAO dao) {
        super(25);
        this.playAtHomeDAO = dao;
        setLayout(new GridBagLayout());
        setBackground(UIStyle.CARD_BG);
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        int row = 0;

        JLabel itemsLabel = new JLabel("Pilihan Barang");
        itemsLabel.setFont(UIStyle.fontBold(18));
        itemsLabel.setForeground(UIStyle.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        add(itemsLabel, gbc);
        gbc.gridwidth = 1;

        kategoriCombo = new RoundedComboBox<>();
        itemCombo = new RoundedComboBox<>();
        jumlahField = new RoundedTextField(5);
        countLabel = new JLabel();
        countLabel.setFont(UIStyle.fontRegular(14));
        countLabel.setForeground(UIStyle.TEXT_LIGHT);

        addField("Pilih Kategori:", kategoriCombo, gbc, row++);

        JPanel itemSelectPanel = new JPanel(new BorderLayout(5, 0));
        itemSelectPanel.setOpaque(false);
        itemSelectPanel.add(itemCombo, BorderLayout.CENTER);
        itemSelectPanel.add(countLabel, BorderLayout.EAST);
        addField("Pilih Barang:", itemSelectPanel, gbc, row++);

        addField("Jumlah:", jumlahField, gbc, row++);

        RoundedButton tambahButton = new RoundedButton("Tambah Barang", 8);
        tambahButton.addActionListener(e -> addItem());

        RoundedButton hapusButton = new RoundedButton("Hapus Barang", 8);
        hapusButton.setBackground(UIStyle.DANGER_COLOR);
        hapusButton.addActionListener(e -> removeItem());

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = row++;
        add(tambahButton, gbc);

        gbc.gridy = row++;
        add(hapusButton, gbc);

        initTable();
        itemScroll = new JScrollPane(itemTable);
        itemScroll.getVerticalScrollBar().setUI(new UIStyle.ModernScrollBarUI());
        itemScroll.setPreferredSize(new Dimension(400, 150));
        itemScroll.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(0, 0, 0, 20), 1, true),
                new EmptyBorder(5, 5, 5, 5)));

        gbc.gridy = row++;
        add(itemScroll, gbc);

        kategoriCombo.addActionListener(e -> loadItems());
        loadCategories();
    }

    private void initTable() {
        itemModel = new DefaultTableModel(new String[] { "Barang", "Jumlah", "Harga Per Hari", "Subtotal" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 1 ? Integer.class : String.class;
            }
        };
        itemTable = new JTable(itemModel);
        UIStyle.styleTable(itemTable);
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

    public void setOnTableChangeListener(Runnable listener) {
        this.onTableChange = listener;
    }

    private void loadCategories() {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return playAtHomeDAO.getCategories();
            }

            @Override
            protected void done() {
                try {
                    for (String cat : get())
                        kategoriCombo.addItem(cat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadItems() {
        String selectedKategori = (String) kategoriCombo.getSelectedItem();
        itemCombo.removeAllItems();
        countLabel.setText("");
        if (selectedKategori == null || selectedKategori.isEmpty())
            return;

        new SwingWorker<Void, Void>() {
            List<String> items;
            int totalCount;

            @Override
            protected Void doInBackground() throws Exception {
                items = playAtHomeDAO.getAvailableItemNames(selectedKategori);
                totalCount = playAtHomeDAO.getAvailableItemCount(selectedKategori);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    for (String item : items)
                        itemCombo.addItem(item);
                    countLabel.setText(" (" + totalCount + " tersedia)");
                    updateAvailableCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void updateAvailableCount() {
        String category = (String) kategoriCombo.getSelectedItem();
        if (category == null)
            return;

        new SwingWorker<Map<String, Integer>, Void>() {
            @Override
            protected Map<String, Integer> doInBackground() throws Exception {
                return playAtHomeDAO.getAvailableItemCountsByName(category);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Integer> counts = get();
                    int totalAvailable = 0;
                    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                        String namaBarang = entry.getKey();
                        int countInDb = entry.getValue();

                        for (int i = 0; i < itemModel.getRowCount(); i++) {
                            if (itemModel.getValueAt(i, 0).toString().equals(namaBarang)) {
                                countInDb -= Integer.parseInt(itemModel.getValueAt(i, 1).toString());
                            }
                        }
                        totalAvailable += countInDb;
                    }
                    countLabel.setText(" (" + totalAvailable + " tersedia)");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void addItem() {
        String selectedItem = (String) itemCombo.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu.", "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahField.getText());
            if (jumlah <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah harus angka > 0.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new SwingWorker<PlayAtHomeDAO.ItemStockInfo, Void>() {
            @Override
            protected PlayAtHomeDAO.ItemStockInfo doInBackground() throws Exception {
                return playAtHomeDAO.getItemStockInfo(selectedItem);
            }

            @Override
            protected void done() {
                try {
                    PlayAtHomeDAO.ItemStockInfo info = get();
                    int inTable = 0;
                    for (int i = 0; i < itemModel.getRowCount(); i++) {
                        if (itemModel.getValueAt(i, 0).equals(selectedItem)) {
                            inTable += (Integer) itemModel.getValueAt(i, 1);
                        }
                    }

                    if (inTable + jumlah > info.totalAvailable) {
                        JOptionPane.showMessageDialog(PlayAtHomeItemPanel.this,
                                "Stok tidak cukup. Tersedia: " + info.totalAvailable, "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    itemModel.addRow(new Object[] { selectedItem, jumlah, info.pricePerDay, 0.0 });
                    updateAvailableCount();
                    jumlahField.setText("");
                    if (onTableChange != null)
                        onTableChange.run();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void removeItem() {
        int row = itemTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih item untuk dihapus.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        itemModel.removeRow(row);
        updateAvailableCount();
        if (onTableChange != null)
            onTableChange.run();
    }

    public void updatePrices(int days) {
        for (int i = 0; i < itemModel.getRowCount(); i++) {
            int qty = (Integer) itemModel.getValueAt(i, 1);
            double price = (Double) itemModel.getValueAt(i, 2);
            itemModel.setValueAt(qty * price * days, i, 3);
        }
    }

    public double getTotalPrice() {
        double total = 0;
        for (int i = 0; i < itemModel.getRowCount(); i++) {
            total += (Double) itemModel.getValueAt(i, 3);
        }
        return total;
    }

    public List<PlayAtHomeDAO.RentalItem> getItems() {
        List<PlayAtHomeDAO.RentalItem> list = new ArrayList<>();
        for (int i = 0; i < itemModel.getRowCount(); i++) {
            String name = (String) itemModel.getValueAt(i, 0);
            int qty = (Integer) itemModel.getValueAt(i, 1);
            double price = (Double) itemModel.getValueAt(i, 2);
            list.add(new PlayAtHomeDAO.RentalItem(name, qty, price));
        }
        return list;
    }

    public void reset() {
        itemModel.setRowCount(0);
        kategoriCombo.setSelectedIndex(0);
        jumlahField.setText("");
        countLabel.setText("");
    }
}
