package tampilan.aset;

import service.AsetDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import tampilan.components.RoundedPanel;
import tampilan.util.UIStyle;

public class AsetManajemenPanel extends JPanel {

    private JTable asetTable;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private TableRowSorter<TableModel> sorter;
    private boolean isEditing = false;
    private JButton editSaveButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JLayeredPane layeredPane;
    private JLabel loadingLabel;
    private AsetDAO asetDAO;

    public AsetManajemenPanel() {
        asetDAO = new AsetDAO();
        setLayout(new BorderLayout(20, 20));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        RoundedPanel titlePanel = new RoundedPanel(15, false);
        titlePanel.setLayout(new BorderLayout(15, 15));
        titlePanel.setBackground(UIStyle.CARD_BG);
        titlePanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = UIStyle.createTitleLabel("Manajemen Data Aset");
        titlePanel.add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        searchField = new JTextField(20);
        UIStyle.styleTextField(searchField);

        filterCombo = new JComboBox<>(
                new String[] { "Semua", "PLAYSTATION 3", "PLAYSTATION 4", "PLAYSTATION 5", "TV 29INCH", "TV 40INCH",
                        "STICK PS4", "STICK PS5", "NINTENDO", "VIP ROOM" });
        UIStyle.styleComboBox(filterCombo);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        searchPanel.add(createLabel("Cari:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        searchPanel.add(searchField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        searchPanel.add(createLabel("Filter Kategori:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.3;
        searchPanel.add(filterCombo, gbc);

        titlePanel.add(searchPanel, BorderLayout.CENTER);
        add(titlePanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[] {
                "ID Aset", "Nama Barang", "Kode Barang", "Kategori",
                "Deskripsi", "Harga/menit", "Harga/hari", "Tersedia", "Disewakan"
        }, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 7 || columnIndex == 8) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return isEditing && column != 0;
            }
        };

        asetTable = new JTable(model);
        UIStyle.styleTable(asetTable);
        asetTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sorter = new TableRowSorter<>(model);
        asetTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(asetTable);
        scrollPane.getVerticalScrollBar().setUI(new UIStyle.ModernScrollBarUI());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        loadingLabel = new JLabel("Memuat data...", SwingConstants.CENTER);
        loadingLabel.setFont(UIStyle.fontBold(18));
        loadingLabel.setForeground(UIStyle.PRIMARY);
        loadingLabel.setOpaque(true);
        loadingLabel.setBackground(new Color(255, 255, 255, 200));
        loadingLabel.setBorder(new EmptyBorder(10, 20, 10, 20));
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

        add(layeredPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        refreshButton = UIStyle.modernButton("\u21BB Refresh");
        refreshButton.setBackground(UIStyle.SECONDARY);
        refreshButton.addActionListener(e -> refreshData());

        editSaveButton = UIStyle.modernButton("\u270E Edit");
        editSaveButton.setBackground(UIStyle.PRIMARY);
        editSaveButton.addActionListener(e -> toggleEditMode());

        deleteButton = UIStyle.modernButton("\uD83D\uDDD1 Hapus");
        deleteButton.setBackground(UIStyle.DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteSelectedRows());

        buttonPanel.add(refreshButton);
        buttonPanel.add(editSaveButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        add(titlePanel, BorderLayout.NORTH);
        add(layeredPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });
        filterCombo.addActionListener(e -> filter());

        loadAsetData();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIStyle.fontMedium(14));
        label.setForeground(UIStyle.TEXT);
        return label;
    }

    private void toggleEditMode() {
        isEditing = !isEditing;

        if (isEditing) {
            editSaveButton.setText("\uD83D\uDCBE Simpan");
            editSaveButton.setBackground(UIStyle.SUCCESS_COLOR);
            deleteButton.setEnabled(false);
            refreshButton.setEnabled(false);
            asetTable.setRowSelectionAllowed(false);
        } else {
            editSaveButton.setText("\u270E Edit");
            editSaveButton.setBackground(UIStyle.PRIMARY);
            deleteButton.setEnabled(true);
            refreshButton.setEnabled(true);
            asetTable.setRowSelectionAllowed(true);
            saveChanges();
        }

        model.fireTableStructureChanged();
    }

    private void saveChanges() {
        if (asetTable.isEditing()) {
            asetTable.getCellEditor().stopCellEditing();
        }

        int rowCount = model.getRowCount();
        if (rowCount == 0)
            return;

        List<Object[]> updates = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            Object[] rowData = new Object[9];
            rowData[0] = model.getValueAt(i, 0); // ID
            rowData[1] = model.getValueAt(i, 1); // Nama
            rowData[2] = model.getValueAt(i, 2); // Kode
            rowData[3] = model.getValueAt(i, 3); // Kategori
            rowData[4] = model.getValueAt(i, 4); // Deskripsi

            // Handle potential String/Double mismatch for prices
            Object hargaMenitObj = model.getValueAt(i, 5);
            if (hargaMenitObj instanceof String) {
                try {
                    rowData[5] = Double.parseDouble((String) hargaMenitObj);
                } catch (NumberFormatException e) {
                    rowData[5] = 0.0; // Default or handle error
                }
            } else {
                rowData[5] = hargaMenitObj;
            }

            Object hargaHariObj = model.getValueAt(i, 6);
            if (hargaHariObj instanceof String) {
                try {
                    rowData[6] = Double.parseDouble((String) hargaHariObj);
                } catch (NumberFormatException e) {
                    rowData[6] = 0.0;
                }
            } else {
                rowData[6] = hargaHariObj;
            }

            rowData[7] = model.getValueAt(i, 7); // Tersedia (Boolean)
            rowData[8] = model.getValueAt(i, 8); // Disewakan (Boolean)

            updates.add(rowData);
        }

        setInteractions(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                asetDAO.updateAset(updates);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIStyle.showSuccessMessage(AsetManajemenPanel.this, "Perubahan berhasil disimpan.");
                } catch (Exception e) {
                    e.printStackTrace();
                    UIStyle.showErrorMessage(AsetManajemenPanel.this, "Gagal menyimpan perubahan: " + e.getMessage());
                } finally {
                    setInteractions(true);
                }
            }
        }.execute();
    }

    private void loadAsetData() {
        setInteractions(false);
        model.setRowCount(0);

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                return asetDAO.getAllAset();
            }

            @Override
            protected void done() {
                try {
                    DefaultTableModel resultModel = get();
                    Vector<String> columnIdentifiers = new Vector<>();
                    String[] columns = { "ID Aset", "Nama Barang", "Kode Barang", "Kategori", "Deskripsi",
                            "Harga/menit", "Harga/hari", "Tersedia", "Disewakan" };
                    for (String column : columns) {
                        columnIdentifiers.add(column);
                    }

                    model.setDataVector(resultModel.getDataVector(), columnIdentifiers);

                    asetTable.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JCheckBox()));
                    asetTable.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(new JCheckBox()));

                } catch (Exception e) {
                    e.printStackTrace();
                    UIStyle.showErrorMessage(AsetManajemenPanel.this, "Gagal memuat data aset: " + e.getMessage());
                } finally {
                    setInteractions(true);
                }
            }
        }.execute();
    }

    private void setInteractions(boolean enabled) {
        searchField.setEnabled(enabled);
        filterCombo.setEnabled(enabled);
        asetTable.setEnabled(enabled);
        editSaveButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
        setCursor(enabled ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loadingLabel.setVisible(!enabled);
    }

    private void filter() {
        String text = searchField.getText();
        String category = filterCombo.getSelectedItem().toString();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        filters.add(RowFilter.regexFilter("(?i)" + text));
        if (!"Semua".equals(category)) {
            filters.add(RowFilter.regexFilter("(?i)" + category, 3));
        }

        sorter.setRowFilter(RowFilter.andFilter(filters));
    }

    private void deleteSelectedRows() {
        int[] selectedRows = asetTable.getSelectedRows();
        if (selectedRows.length == 0) {
            UIStyle.showSuccessMessage(this, "Pilih baris yang ingin dihapus.");
            return;
        }

        List<String> idsToDelete = new ArrayList<>();
        List<String> namesToDelete = new ArrayList<>();

        for (int viewRow : selectedRows) {
            int modelRow = asetTable.convertRowIndexToModel(viewRow);
            idsToDelete.add(model.getValueAt(modelRow, 0).toString());
            namesToDelete.add(model.getValueAt(modelRow, 1).toString());
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus " + selectedRows.length + " aset berikut?\n" +
                        String.join(", ", namesToDelete) +
                        "\n\nPERINGATAN: Semua riwayat booking dan penyewaan terkait aset ini juga akan dihapus.",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            setInteractions(false);
            new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return asetDAO.deleteAset(idsToDelete);
                }

                @Override
                protected void done() {
                    try {
                        int successCount = get();
                        UIStyle.showSuccessMessage(AsetManajemenPanel.this,
                                "Berhasil menghapus " + successCount + " aset beserta data terkaitnya.");
                        refreshData();
                    } catch (Exception e) {
                        e.printStackTrace();
                        UIStyle.showErrorMessage(AsetManajemenPanel.this, "Gagal menghapus data: " + e.getMessage());
                    } finally {
                        setInteractions(true);
                    }
                }
            }.execute();
        }
    }

    public void refreshData() {
        loadAsetData();
    }
}