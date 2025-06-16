package form;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import Database.DatabaseConnection;

public class MenuMakanan extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> comboKategori;

    public MenuMakanan() {
        setTitle("Daftar Makanan & Minuman");
        setSize(650, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panelUtama = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bg = new ImageIcon(getClass().getResource("/images/background_login.jpg"));
                g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        panelUtama.setLayout(new BorderLayout());
        panelUtama.setOpaque(false);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Nama", "Harga", "Stok"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        comboKategori = new JComboBox<>(new String[]{"Semua", "Makanan", "Minuman"});
        comboKategori.addActionListener(e -> loadData());
        topPanel.add(new JLabel("Kategori:"));
        topPanel.add(comboKategori);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        JButton btnTambah = new JButton("Tambah");
        JButton btnEdit = new JButton("Edit");
        JButton btnHapus = new JButton("Hapus");
        JButton btnKembali = new JButton("Kembali");
        JButton btnReset = new JButton("Reset");

        btnTambah.addActionListener(e -> tambahBarang());
        btnEdit.addActionListener(e -> editBarang());
        btnHapus.addActionListener(e -> hapusBarang());
        btnReset.addActionListener(e -> resetBarang());
        btnKembali.addActionListener(e -> {
            dispose();
            new MainMenu();
        });

        buttonPanel.add(btnKembali);
        buttonPanel.add(btnTambah);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnReset);

        panelUtama.add(topPanel, BorderLayout.NORTH);
        panelUtama.add(scrollPane, BorderLayout.CENTER);
        panelUtama.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(panelUtama);
        loadData();
        setVisible(true);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String selectedKategori = (String) comboKategori.getSelectedItem();

        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM barang");

            while (rs.next()) {
                String namaAsli = rs.getString("nama_barang");

                if (selectedKategori.equals("Makanan") && !namaAsli.toLowerCase().contains("makan")) continue;
                if (selectedKategori.equals("Minuman") && !namaAsli.toLowerCase().contains("minum")) continue;

                int id = rs.getInt("id_barang");
                int harga = rs.getInt("harga");
                int stok = rs.getInt("stok");

                String namaTampil = namaAsli.replaceAll("(?i)makanan", "")
                        .replaceAll("(?i)minuman", "")
                        .trim();

                tableModel.addRow(new Object[]{id, namaTampil, harga, stok});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal load data: " + e.getMessage());
        }
    }


    private void tambahBarang() {
        String nama = JOptionPane.showInputDialog(this, "Masukkan nama barang:");
        String hargaStr = JOptionPane.showInputDialog(this, "Masukkan harga:");
        String stokStr = JOptionPane.showInputDialog(this, "Masukkan stok:");

        if (nama == null || hargaStr == null || stokStr == null) return;

        try {
            int harga = Integer.parseInt(hargaStr);
            int stok = Integer.parseInt(stokStr);

            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO barang (nama_barang, harga, stok) VALUES (?, ?, ?)");
            stmt.setString(1, nama);
            stmt.setInt(2, harga);
            stmt.setInt(3, stok);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Barang berhasil ditambahkan!");
            loadData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal tambah barang: " + e.getMessage());
        }
    }

    private void editBarang() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih barang yang mau diedit!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String nama = JOptionPane.showInputDialog(this, "Edit nama:", tableModel.getValueAt(selectedRow, 1));
        String hargaStr = JOptionPane.showInputDialog(this, "Edit harga:", tableModel.getValueAt(selectedRow, 2));
        String stokStr = JOptionPane.showInputDialog(this, "Edit stok:", tableModel.getValueAt(selectedRow, 3));

        try {
            int harga = Integer.parseInt(hargaStr);
            int stok = Integer.parseInt(stokStr);

            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE barang SET nama_barang=?, harga=?, stok=? WHERE id_barang=?");
            stmt.setString(1, nama);
            stmt.setInt(2, harga);
            stmt.setInt(3, stok);
            stmt.setInt(4, id);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Barang berhasil diupdate!");
            loadData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal edit barang: " + e.getMessage());
        }
    }

    private void hapusBarang() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih barang yang mau dihapus!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin mau dihapus?", "Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM barang WHERE id_barang=?");
                stmt.setInt(1, id);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Barang berhasil dihapus!");
                loadData();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal hapus barang: " + e.getMessage());
            }
        }
    }

    private void resetBarang() {
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin reset semua barang?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("TRUNCATE TABLE barang");
                JOptionPane.showMessageDialog(null, "Data barang sudah di-reset dan ID mulai dari 1");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Gagal reset barang: " + e.getMessage());
            }
        }
    }

    private void orderItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu barang yang mau diorder!");
            return;
        }

        int idBarang = (int) tableModel.getValueAt(selectedRow, 0);
        String nama = (String) tableModel.getValueAt(selectedRow, 1);
        int harga = (int) tableModel.getValueAt(selectedRow, 2);
        int stok = (int) tableModel.getValueAt(selectedRow, 3);

        String jumlahStr = JOptionPane.showInputDialog(this, "Masukkan jumlah:");
        if (jumlahStr == null || jumlahStr.isEmpty()) return;

        try {
            int jumlah = Integer.parseInt(jumlahStr);
            if (jumlah > stok) {
                JOptionPane.showMessageDialog(this, "Stok tidak mencukupi!");
                return;
            }

            int total = harga * jumlah;

            Connection conn = DatabaseConnection.getConnection();

            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO transak (id_barang, nama_barang, harga, jumlah, subtotal) VALUES (?, ?, ?, ?, ?)");
            stmt.setInt(1, idBarang);
            stmt.setString(2, nama);
            stmt.setInt(3, harga);
            stmt.setInt(4, jumlah);
            stmt.setInt(5, total);
            stmt.executeUpdate();

            PreparedStatement updateStok = conn.prepareStatement(
                    "UPDATE barang SET stok = stok - ? WHERE id_barang = ?");
            updateStok.setInt(1, jumlah);
            updateStok.setInt(2, idBarang);
            updateStok.executeUpdate();

            JOptionPane.showMessageDialog(this, "Berhasil diorder! Total: " + total);
            loadData();
            dispose();
            new RiwayatPembelian();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Jumlah harus angka!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MenuMakanan::new);
    }
}