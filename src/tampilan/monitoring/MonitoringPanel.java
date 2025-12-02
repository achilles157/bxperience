package tampilan.monitoring;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import tampilan.util.UIStyle;
import service.MonitoringDAO;
import tampilan.components.RoundedButton;
import tampilan.components.RoundedPanel;

public class MonitoringPanel extends JPanel {

    private JTable table;
    private JLabel totalTersediaLabel;
    private JLabel totalDisewaLabel;
    private JLabel totalBookingLabel;
    private DefaultTableModel tableModel;

    public MonitoringPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        RoundedPanel headerPanel = new RoundedPanel(20, false);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(UIStyle.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel titleLabel = new JLabel("MONITORING PENYEWAAN", SwingConstants.CENTER);
        titleLabel.setFont(UIStyle.fontBold(24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIStyle.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        mainPanel.add(createSummaryPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(createTablePanel());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUI(new UIStyle.ModernScrollBarUI());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        RoundedButton playHomeButton = new RoundedButton("Data Play At Home", 10);
        playHomeButton.setBackground(UIStyle.PRIMARY);
        playHomeButton.setForeground(Color.WHITE);
        playHomeButton.setFont(UIStyle.fontBold(14));
        playHomeButton.setPreferredSize(new Dimension(200, 45));
        playHomeButton.addActionListener(e -> {
            javaapplication1.MainFrame.setPage(
                    new tampilan.utama.SidebarShell(new tampilan.playathome.PlayAtHomeRentalPanel()),
                    "playathome-rentals");
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        bottomPanel.add(playHomeButton);

        add(bottomPanel, BorderLayout.SOUTH);

        loadSummaryData();
        loadBookingData();
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        totalTersediaLabel = new JLabel(
                "<html><div style='text-align:center'>Tersedia<br><span style='font-size:24px'>0</span></div></html>",
                SwingConstants.CENTER);
        totalDisewaLabel = new JLabel(
                "<html><div style='text-align:center'>Disewa<br><span style='font-size:24px'>0</span></div></html>",
                SwingConstants.CENTER);
        totalBookingLabel = new JLabel(
                "<html><div style='text-align:center'>Booking<br><span style='font-size:24px'>0</span></div></html>",
                SwingConstants.CENTER);

        JLabel[] labels = { totalTersediaLabel, totalDisewaLabel, totalBookingLabel };
        Color[] colors = { UIStyle.SUCCESS_COLOR, UIStyle.WARNING_COLOR, UIStyle.PRIMARY };

        for (int i = 0; i < labels.length; i++) {
            JLabel label = labels[i];
            label.setFont(UIStyle.fontBold(16));
            label.setForeground(Color.WHITE);
            label.setOpaque(false);

            RoundedPanel card = new RoundedPanel(15, true);
            card.setLayout(new BorderLayout());
            card.setBackground(colors[i]);
            card.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
            card.setPreferredSize(new Dimension(200, 100));
            card.setMaximumSize(new Dimension(300, 120));
            card.add(label, BorderLayout.CENTER);

            panel.add(card);
            if (i < labels.length - 1) {
                panel.add(Box.createRigidArea(new Dimension(15, 0)));
            }
        }

        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = { "ID Booking", "Nama Pelanggan", "Item", "Tanggal", "Jam", "Durasi", "Status", "Aksi" };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Hanya kolom aksi yang bisa di-edit
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 7 ? JButton.class : Object.class;
            }
        };

        table = new JTable(tableModel);
        UIStyle.styleTable(table);
        table.getColumn("Aksi").setCellRenderer(new ButtonRenderer());
        table.getColumn("Aksi").setCellEditor(new ButtonEditor(new JCheckBox()));
        table.setRowHeight(45);

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getVerticalScrollBar().setUI(new UIStyle.ModernScrollBarUI());
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(0, 0, 0, 20), 1, true),
                new EmptyBorder(5, 5, 5, 5)));

        return scrollPane;
    }

    private void loadSummaryData() {

        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                MonitoringDAO dao = new MonitoringDAO();
                return dao.getSummaryData();
            }

            @Override
            protected void done() {
                try {
                    // Ambil hasil dan perbarui UI di Event Dispatch Thread
                    int[] summary = get();
                    totalTersediaLabel
                            .setText("<html><div style='text-align:center'>Tersedia<br><span style='font-size:24px'>"
                                    + summary[0] + "</span></div></html>");
                    totalDisewaLabel
                            .setText("<html><div style='text-align:center'>Disewa<br><span style='font-size:24px'>"
                                    + summary[1] + "</span></div></html>");
                    totalBookingLabel
                            .setText("<html><div style='text-align:center'>Booking<br><span style='font-size:24px'>"
                                    + summary[2] + "</span></div></html>");
                } catch (Exception e) {
                    e.printStackTrace();
                    UIStyle.showErrorMessage(MonitoringPanel.this, "Gagal memuat data summary: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void loadBookingData() {
        tableModel.setRowCount(0);

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                MonitoringDAO dao = new MonitoringDAO();
                return dao.getBookingData();
            }

            @Override
            protected void done() {
                try {
                    DefaultTableModel resultModel = get();

                    java.util.Vector<String> columnIdentifiers = new java.util.Vector<>();
                    String[] columns = { "ID Booking", "Nama Pelanggan", "Item", "Tanggal", "Jam", "Durasi", "Status",
                            "Aksi" };
                    for (String column : columns) {
                        columnIdentifiers.add(column);
                    }

                    tableModel.setDataVector(resultModel.getDataVector(), columnIdentifiers);

                    table.getColumn("Aksi").setCellRenderer(new ButtonRenderer());
                    table.getColumn("Aksi").setCellEditor(new ButtonEditor(new JCheckBox()));

                    table.getColumnModel().getColumn(0).setPreferredWidth(100);
                    table.getColumnModel().getColumn(1).setPreferredWidth(150);
                    table.getColumnModel().getColumn(2).setPreferredWidth(200);
                    table.getColumnModel().getColumn(3).setPreferredWidth(100);
                    table.getColumnModel().getColumn(4).setPreferredWidth(80);
                    table.getColumnModel().getColumn(5).setPreferredWidth(80);
                    table.getColumnModel().getColumn(6).setPreferredWidth(80);
                    table.getColumnModel().getColumn(7).setPreferredWidth(100);

                } catch (Exception e) {
                    e.printStackTrace();
                    UIStyle.showErrorMessage(MonitoringPanel.this, "Gagal memuat data transaksi: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void kembalikanItem(String idTransaksi) {
        try {
            MonitoringDAO dao = new MonitoringDAO();
            dao.kembalikanItem(idTransaksi);

            UIStyle.showSuccessMessage(this, "Item berhasil dikembalikan");

            loadSummaryData();
            loadBookingData();

        } catch (SQLException e) {
            e.printStackTrace();
            UIStyle.showErrorMessage(this, "Gagal mengembalikan item: " + e.getMessage());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            UIStyle.showErrorMessage(this, "Format ID transaksi tidak valid");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            UIStyle.showErrorMessage(this, e.getMessage());
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(UIStyle.SUCCESS_COLOR);
            setForeground(Color.WHITE);
            setFont(UIStyle.fontBold(12));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Kembalikan" : value.toString());

            String status = tableModel.getValueAt(row, 6).toString();
            if ("Aktif".equals(status)) {
                setBackground(UIStyle.WARNING_COLOR);
            } else {
                setBackground(UIStyle.SUCCESS_COLOR);
            }

            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new RoundedButton("Kembalikan", 6);
            button.setBackground(UIStyle.SUCCESS_COLOR);
            button.setForeground(Color.WHITE);
            button.setFont(UIStyle.fontBold(12));
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "Kembalikan" : value.toString();
            button.setText(label);

            String status = tableModel.getValueAt(row, 6).toString();
            if ("Aktif".equals(status)) {
                button.setBackground(UIStyle.WARNING_COLOR);
            } else {
                button.setBackground(UIStyle.SUCCESS_COLOR);
            }

            this.row = row;
            clicked = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {

                String transactionId = tableModel.getValueAt(row, 0).toString();
                String nama = tableModel.getValueAt(row, 1).toString();
                String items = tableModel.getValueAt(row, 2).toString();

                JPanel panel = new JPanel(new BorderLayout(10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JLabel iconLabel = new JLabel();
                iconLabel.setPreferredSize(new Dimension(48, 48));
                iconLabel.setOpaque(true);
                iconLabel.setBackground(UIStyle.WARNING_COLOR);
                iconLabel.setBorder(BorderFactory.createLineBorder(UIStyle.WARNING_COLOR.darker(), 2));
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                iconLabel.setForeground(Color.WHITE);
                iconLabel.setFont(UIStyle.fontBold(24));
                iconLabel.setText("!");

                JLabel messageLabel = new JLabel("<html><div style='width: 300px;'>" +
                        "<b>Konfirmasi Pengembalian</b><br><br>" +
                        "Anda akan mengembalikan transaksi berikut:<br>" +
                        "ID: " + transactionId + "<br>" +
                        "Nama: " + nama + "<br>" +
                        "Items: " + items + "<br><br>" +
                        "Apakah Anda yakin?</div></html>");
                messageLabel.setFont(UIStyle.fontRegular(14));

                panel.add(iconLabel, BorderLayout.WEST);
                panel.add(messageLabel, BorderLayout.CENTER);

                int confirm = JOptionPane.showOptionDialog(
                        button,
                        panel,
                        "Konfirmasi Pengembalian",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new Object[] { "Ya, Kembalikan", "Batal" },
                        "Batal");

                if (confirm == JOptionPane.YES_OPTION) {
                    kembalikanItem(transactionId);
                }
            }
            clicked = false;
            return label;
        }

        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}