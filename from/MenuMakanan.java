package form;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import Database.DatabaseConnection;

public class MenuMakanan extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;

    public MenuMakanan() {
        setTitle("Daftar Makanan");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Nama", "Harga", "Stok"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel panelButton = new JPanel();
        JButton btnTambah = new JButton("Tambah");
        JButton btnEdit = new JButton("Edit");
        JButton btnHapus = new JButton("Hapus");
        JButton btnOrder = new JButton("Order");

        btnTambah.addActionListener(e -> tambahBarang());
        btnEdit.addActionListener(e -> editBarang());
        btnHapus.addActionListener(e -> hapusBarang());
        btnOrder.addActionListener(e -> orderItem());

        panelButton.add(btnTambah);
        panelButton.add(btnEdit);
        panelButton.add(btnHapus);
        panelButton.add(btnOrder);

        add(scrollPane, BorderLayout.CENTER);
        add(panelButton, BorderLayout.SOUTH);

        loadData();
        setVisible(true);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM barang");

            while (rs.next()) {
                int id = rs.getInt("id_barang");
                String nama = rs.getString("nama_barang");
                int harga = rs.getInt("harga");
                int stok = rs.getInt("stok");
                tableModel.addRow(new Object[]{id, nama, harga, stok});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal load data: " + e.getMessage());
        }
    }

    private void tambahBarang() {
        JTextField namaField = new JTextField();
        JTextField hargaField = new JTextField();
        JTextField stokField = new JTextField();

        Object[] input = {
                "Nama Barang:", namaField,
                "Harga:", hargaField,
                "Stok:", stokField
        };

        int result = JOptionPane.showConfirmDialog(this, input, "Tambah Barang", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nama = namaField.getText();
                int harga = Integer.parseInt(hargaField.getText());
                int stok = Integer.parseInt(stokField.getText());

                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO barang (nama_barang, harga, stok) VALUES (?, ?, ?)");
                stmt.setString(1, nama);
                stmt.setInt(2, harga);
                stmt.setInt(3, stok);
                stmt.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Input tidak valid atau gagal menyimpan!");
            }
        }
    }

    private void editBarang() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu data yang ingin diedit!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String namaLama = (String) tableModel.getValueAt(selectedRow, 1);
        int hargaLama = (int) tableModel.getValueAt(selectedRow, 2);
        int stokLama = (int) tableModel.getValueAt(selectedRow, 3);

        JTextField namaField = new JTextField(namaLama);
        JTextField hargaField = new JTextField(String.valueOf(hargaLama));
        JTextField stokField = new JTextField(String.valueOf(stokLama));

        Object[] input = {
                "Nama Barang:", namaField,
                "Harga:", hargaField,
                "Stok:", stokField
        };

        int result = JOptionPane.showConfirmDialog(this, input, "Edit Barang", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nama = namaField.getText();
                int harga = Integer.parseInt(hargaField.getText());
                int stok = Integer.parseInt(stokField.getText());

                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("UPDATE barang SET nama_barang=?, harga=?, stok=? WHERE id_barang=?");
                stmt.setString(1, nama);
                stmt.setInt(2, harga);
                stmt.setInt(3, stok);
                stmt.setInt(4, id);
                stmt.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, "Data berhasil diedit!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Input tidak valid atau gagal menyimpan!");
            }
        }
    }

    private void hapusBarang() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu data yang ingin dihapus!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus barang ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM barang WHERE id_barang=?");
                stmt.setInt(1, id);
                stmt.executeUpdate();
                loadData();
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data!");
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

        String jumlahStr = JOptionPane.showInputDialog(this, "Masukkan jumlah:");
        if (jumlahStr == null || jumlahStr.isEmpty()) return;

        try {
            int jumlah = Integer.parseInt(jumlahStr);
            int subtotal = harga * jumlah;

            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO transak (id_barang, nama_barang, harga, jumlah, subtotal, tanggal) VALUES (?, ?, ?, ?, ?, CURRENT_DATE)"
            );
            stmt.setInt(1, idBarang);
            stmt.setString(2, nama);
            stmt.setInt(3, harga);
            stmt.setInt(4, jumlah);
            stmt.setInt(5, subtotal);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Berhasil diorder! Subtotal: " + subtotal);

            // Tampilkan Transaksi setelah order
            new Transaksi();

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