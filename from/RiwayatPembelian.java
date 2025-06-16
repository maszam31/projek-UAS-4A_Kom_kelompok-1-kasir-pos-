package form;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import Database.DatabaseConnection;

public class RiwayatPembelian extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblHari, lblMinggu, lblBulan, lblTahun;

    public RiwayatPembelian() {
        setTitle("Riwayat Pembelian");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Panel utama dengan background gambar
        JPanel mainPanel = new JPanel() {
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/background_menu.jpg"));
            Image img = icon.getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Tabel transaksi
        tableModel = new DefaultTableModel(new Object[]{
                "ID", "ID Barang", "Nama", "Harga", "Jumlah", "Subtotal", "Tanggal"
        }, 0);
        table = new JTable(tableModel);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionBackground(new Color(200, 230, 255));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBackground(Color.WHITE);

        // Panel ringkasan pendapatan
        JPanel summaryPanel = new JPanel(new GridLayout(2, 4, 10, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Total Pendapatan"));
        summaryPanel.setBackground(Color.WHITE);

        JLabel lbl1 = new JLabel("Hari Ini:");
        JLabel lbl2 = new JLabel("Minggu Ini:");
        JLabel lbl3 = new JLabel("Bulan Ini:");
        JLabel lbl4 = new JLabel("Tahun Ini:");

        lbl1.setForeground(Color.BLACK);
        lbl2.setForeground(Color.BLACK);
        lbl3.setForeground(Color.BLACK);
        lbl4.setForeground(Color.BLACK);

        lblHari = new JLabel();
        lblMinggu = new JLabel();
        lblBulan = new JLabel();
        lblTahun = new JLabel();

        lblHari.setForeground(Color.BLACK);
        lblMinggu.setForeground(Color.BLACK);
        lblBulan.setForeground(Color.BLACK);
        lblTahun.setForeground(Color.BLACK);

        summaryPanel.add(lbl1);
        summaryPanel.add(lbl2);
        summaryPanel.add(lbl3);
        summaryPanel.add(lbl4);
        summaryPanel.add(lblHari);
        summaryPanel.add(lblMinggu);
        summaryPanel.add(lblBulan);
        summaryPanel.add(lblTahun);

        // Panel tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton btnHapus = new JButton("Hapus");
        JButton btnKembali = new JButton("Kembali");
        JButton btnReset = new JButton("Reset");

        btnHapus.addActionListener(e -> hapusTransaksi());
        btnKembali.addActionListener(e -> kembaliKeMenu());
        btnReset.addActionListener(e -> resetRiwayat());

        buttonPanel.add(btnKembali);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnReset);

        // Gabungkan panel ringkasan dan tombol
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(summaryPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Tambahkan semua ke main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Load data dan hitung total
        loadData();
        updateTotalPendapatan();

        setVisible(true);
    }

    private void kembaliKeMenu() {
        dispose();
        new MainMenu();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM transak");

            while (rs.next()) {
                int harga = rs.getInt("harga");
                int subtotal = rs.getInt("subtotal");

                Object[] row = {
                        rs.getInt("id_transaksi"),
                        rs.getInt("id_barang"),
                        rs.getString("nama_barang"),
                        formatRupiah(harga),
                        rs.getInt("jumlah"),
                        formatRupiah(subtotal),
                        rs.getTimestamp("tanggal")
                };

                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal load data: " + e.getMessage());
        }
    }

    private void hapusTransaksi() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi yang mau dihapus!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin mau hapus transaksi ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM transak WHERE id_transaksi = ?");
                stmt.setInt(1, id);
                stmt.executeUpdate();
                loadData();
                updateTotalPendapatan();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal hapus transaksi: " + e.getMessage());
            }
        }
    }

    private void resetRiwayat() {
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin reset semua riwayat?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("TRUNCATE TABLE transak");

                JOptionPane.showMessageDialog(this, "Riwayat berhasil direset!");
                loadData();
                updateTotalPendapatan();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal mereset riwayat: " + e.getMessage());
            }
        }
    }

    private void updateTotalPendapatan() {
        lblHari.setText(formatRupiah(getTotalPendapatan("harian")));
        lblMinggu.setText(formatRupiah(getTotalPendapatan("mingguan")));
        lblBulan.setText(formatRupiah(getTotalPendapatan("bulanan")));
        lblTahun.setText(formatRupiah(getTotalPendapatan("tahunan")));
    }

    private int getTotalPendapatan(String periode) {
        int total = 0;
        String query = "";

        switch (periode) {
            case "harian":
                query = "SELECT SUM(subtotal) FROM transak WHERE DATE(tanggal) = CURDATE()";
                break;
            case "mingguan":
                query = "SELECT SUM(subtotal) FROM transak WHERE YEARWEEK(tanggal, 1) = YEARWEEK(CURDATE(), 1)";
                break;
            case "bulanan":
                query = "SELECT SUM(subtotal) FROM transak WHERE MONTH(tanggal) = MONTH(CURDATE()) AND YEAR(tanggal) = YEAR(CURDATE())";
                break;
            case "tahunan":
                query = "SELECT SUM(subtotal) FROM transak WHERE YEAR(tanggal) = YEAR(CURDATE())";
                break;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil total pendapatan: " + e.getMessage());
        }

        return total;
    }

    private String formatRupiah(int amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return nf.format(amount);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RiwayatPembelian::new);
    }
}