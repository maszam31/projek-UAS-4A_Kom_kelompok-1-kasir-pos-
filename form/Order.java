package form;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;

import Database.DatabaseConnection;

public class Order extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;

    private JComboBox<String> cbBarang;
    private JTextField tfJumlah;
    private JTextField tfUangBayar;
    private JLabel lblKembalian;
    private JLabel lblTotalHarga;
    private JButton btnTambah;

    private Vector<Item> daftarBarang = new Vector<>();

    private static class Item {
        int id;
        String nama;
        int harga;

        Item(int id, String nama, int harga) {
            this.id = id;
            this.nama = nama;
            this.harga = harga;
        }

        @Override
        public String toString() {
            return nama + " - Rp " + harga;
        }
    }

    public Order() {
        setTitle("Data Transaksi");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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

        loadDaftarBarang();

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setOpaque(false);

        cbBarang = new JComboBox<>();
        for (Item item : daftarBarang) {
            cbBarang.addItem(item.toString());
        }

        tfJumlah = new JTextField("1", 3);
        tfJumlah.setHorizontalAlignment(JTextField.CENTER);

        JButton btnNaik = new JButton("▲");
        JButton btnTurun = new JButton("▼");

        btnNaik.addActionListener(e -> {
            try {
                int jumlah = Integer.parseInt(tfJumlah.getText());
                tfJumlah.setText(String.valueOf(jumlah + 1));
            } catch (NumberFormatException ex) {
                tfJumlah.setText("1");
            }
        });

        btnTurun.addActionListener(e -> {
            try {
                int jumlah = Integer.parseInt(tfJumlah.getText());
                if (jumlah > 1) {
                    tfJumlah.setText(String.valueOf(jumlah - 1));
                }
            } catch (NumberFormatException ex) {
                tfJumlah.setText("1");
            }
        });

        JPanel jumlahPanel = new JPanel(new BorderLayout());
        jumlahPanel.add(tfJumlah, BorderLayout.CENTER);
        JPanel tombolPanel = new JPanel(new GridLayout(2, 1));
        tombolPanel.add(btnNaik);
        tombolPanel.add(btnTurun);
        jumlahPanel.add(tombolPanel, BorderLayout.EAST);

        btnTambah = new JButton("Tambah");

        inputPanel.add(new JLabel("Barang:") {{ setForeground(Color.WHITE); }});
        inputPanel.add(cbBarang);
        inputPanel.add(new JLabel("Jumlah:") {{ setForeground(Color.WHITE); }});
        inputPanel.add(jumlahPanel);
        inputPanel.add(btnTambah);


        tableModel = new DefaultTableModel(new Object[]{
                "ID Barang", "Nama", "Harga", "Jumlah", "Subtotal"
        }, 0);

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(Color.WHITE); // Latar belakang putih untuk sel yang tidak dipilih
                } else {
                    c.setBackground(new Color(184, 207, 229)); // Warna default saat dipilih
                }
                return c;
            }
        };
        scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);


        JPanel pembayaranPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pembayaranPanel.setOpaque(false);

        tfUangBayar = new JTextField(10);

        lblKembalian = new JLabel("Kembalian: Rp 0");
        lblKembalian.setForeground(Color.WHITE);

        lblTotalHarga = new JLabel("Total Harga: Rp 0");
        lblTotalHarga.setForeground(Color.WHITE);

        pembayaranPanel.add(new JLabel("Uang Bayar: Rp") {{
            setForeground(Color.WHITE);
        }});
        pembayaranPanel.add(tfUangBayar);
        pembayaranPanel.add(lblKembalian);
        pembayaranPanel.add(lblTotalHarga);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnHapus = new JButton("Hapus Baris");
        JButton btnPrint = new JButton("Cetak Struk");
        JButton btnKembali = new JButton("Kembali");
        buttonPanel.add(btnKembali);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnPrint);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(pembayaranPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        btnTambah.addActionListener(e -> tambahItemKeTabel());
        btnHapus.addActionListener(e -> hapusBarisTabel());
        btnKembali.addActionListener(e -> kembaliKeMenu());
        btnPrint.addActionListener(e -> simpanTransaksiDanCetakPDF());

        tfUangBayar.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { hitungKembalian(); }
            public void removeUpdate(DocumentEvent e) { hitungKembalian(); }
            public void insertUpdate(DocumentEvent e) { hitungKembalian(); }
        });

        setVisible(true);
    }

    private void loadDaftarBarang() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_barang, nama_barang, harga FROM barang");
            daftarBarang.clear();
            while (rs.next()) {
                String nama = rs.getString("nama_barang");
                nama = nama.replaceAll("(?i)\\b(Makanan|Minuman)\\b", "").trim();

                daftarBarang.add(new Item(
                        rs.getInt("id_barang"),
                        nama,
                        rs.getInt("harga")
                ));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal load daftar barang: " + e.getMessage());
        }
    }

    private void tambahItemKeTabel() {
        int selectedIndex = cbBarang.getSelectedIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "Pilih barang dulu!");
            return;
        }
        int jumlah;
        try {
            jumlah = Integer.parseInt(tfJumlah.getText());
            if (jumlah <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Jumlah harus angka positif!");
            return;
        }

        Item item = daftarBarang.get(selectedIndex);

        boolean sudahAda = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int idBarang = (int) tableModel.getValueAt(i, 0);
            if (idBarang == item.id) {
                int jumlahLama = (int) tableModel.getValueAt(i, 3);
                int jumlahBaru = jumlahLama + jumlah;
                int subtotalBaru = jumlahBaru * item.harga;
                tableModel.setValueAt(jumlahBaru, i, 3);
                tableModel.setValueAt(subtotalBaru, i, 4);
                sudahAda = true;
                break;
            }
        }
        if (!sudahAda) {
            int subtotal = jumlah * item.harga;
            tableModel.addRow(new Object[]{
                    item.id, item.nama, item.harga, jumlah, subtotal
            });
        }
        hitungKembalian();
    }

    private void hapusBarisTabel() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih baris yang mau dihapus!");
            return;
        }
        tableModel.removeRow(selectedRow);
        hitungKembalian();
    }

    private void hitungKembalian() {
        int total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += (int) tableModel.getValueAt(i, 4);
        }
        lblTotalHarga.setText("Total Harga: Rp " + total);

        int uangBayar;
        try {
            uangBayar = Integer.parseInt(tfUangBayar.getText());
        } catch (NumberFormatException e) {
            lblKembalian.setText("Kembalian: Rp 0");
            return;
        }

        int kembali = uangBayar - total;
        if (kembali < 0) {
            lblKembalian.setText("Uang kurang Rp " + (-kembali));
        } else {
            lblKembalian.setText("Kembalian: Rp " + kembali);
        }
    }

    private void simpanTransaksiDanCetakPDF() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Belum ada barang yang ditambahkan!");
            return;
        }

        int uangBayar;
        try {
            uangBayar = Integer.parseInt(tfUangBayar.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Masukkan jumlah uang yang valid!");
            return;
        }

        int total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += (int) tableModel.getValueAt(i, 4);
        }

        if (uangBayar < total) {
            JOptionPane.showMessageDialog(this, "Uang bayar kurang dari total!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO transak (id_barang, nama_barang, harga, jumlah, subtotal, tanggal) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                    Statement.RETURN_GENERATED_KEYS
            );

            Vector<Object[]> listTransaksi = new Vector<>();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object[] row = new Object[6];
                row[0] = tableModel.getValueAt(i, 0);
                row[1] = tableModel.getValueAt(i, 1);
                row[2] = tableModel.getValueAt(i, 2);
                row[3] = tableModel.getValueAt(i, 3);
                row[4] = tableModel.getValueAt(i, 4);
                row[5] = new Timestamp(System.currentTimeMillis());

                stmt.setInt(1, (int) row[0]);
                stmt.setString(2, (String) row[1]);
                stmt.setInt(3, (int) row[2]);
                stmt.setInt(4, (int) row[3]);
                stmt.setInt(5, (int) row[4]);
                stmt.addBatch();

                listTransaksi.add(row);
            }

            stmt.executeBatch();
            PreparedStatement updateStokStmt = conn.prepareStatement(
                    "UPDATE barang SET stok = stok - ? WHERE id_barang = ?"
            );

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int idBarang = (int) tableModel.getValueAt(i, 0);
                int jumlah = (int) tableModel.getValueAt(i, 3);

                updateStokStmt.setInt(1, jumlah);
                updateStokStmt.setInt(2, idBarang);
                updateStokStmt.addBatch();
            }

            updateStokStmt.executeBatch();

            conn.commit();

            cetakStrukPDF(listTransaksi, uangBayar, total);

            tableModel.setRowCount(0);
            tfUangBayar.setText("");
            lblKembalian.setText("Kembalian: Rp 0");
            lblTotalHarga.setText("Total Harga: Rp 0");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal simpan transaksi: " + e.getMessage());
        }
    }

    private void kembaliKeMenu() {
        dispose();
        new MainMenu();
    }

    private void cetakStrukPDF(Vector<Object[]> listTransaksi, int uangBayar, int total) {
        try {
            String folderPath = "D:\\projek4Akom\\projek4Akom\\src\\stuk";
            File dir = new File(folderPath);
            if (!dir.exists()) dir.mkdirs();

            String waktuFile = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String filename = folderPath + "\\transaksi_" + waktuFile + ".pdf";

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filename));

            Font fontHeader = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fontBody = new Font(Font.FontFamily.HELVETICA, 12);

            document.open();
            document.add(new Paragraph("STRUK TRANSAKSI", fontHeader));
            document.add(new Paragraph("----------------------------------------------------", fontBody));

            for (Object[] row : listTransaksi) {
                document.add(new Paragraph("ID Barang     : " + row[0], fontBody));
                document.add(new Paragraph("Nama Barang   : " + row[1], fontBody));
                document.add(new Paragraph("Harga         : Rp " + row[2], fontBody));
                document.add(new Paragraph("Jumlah        : " + row[3], fontBody));
                document.add(new Paragraph("Subtotal      : Rp " + row[4], fontBody));
                document.add(new Paragraph("", fontBody));
            }

            document.add(new Paragraph("----------------------------------------------------", fontBody));
            document.add(new Paragraph("Total         : Rp " + total, fontBody));
            document.add(new Paragraph("Uang Bayar    : Rp " + uangBayar, fontBody));
            document.add(new Paragraph("Kembalian     : Rp " + (uangBayar - total), fontBody));

            String tanggal = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new java.util.Date());
            document.add(new Paragraph("Tanggal       : " + tanggal, fontBody));
            document.add(new Paragraph("----------------------------------------------------", fontBody));
            document.add(new Paragraph("\nTERIMAKASIH TELAH BERBELANJA", fontBody));
            document.add(new Paragraph("\n SELAMAT BERBELANJA KEMBALI", fontBody));

            document.close();

            // Mencetak file PDF secara otomatis
            File pdfFile = new File(filename);
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.print(pdfFile);
                    } catch (Exception printEx) {
                        JOptionPane.showMessageDialog(this, "Struk berhasil disimpan, tapi gagal mencetak:\n" + printEx.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Struk berhasil disimpan, tapi fitur print tidak didukung OS.");
                }
            }

            JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan dan struk dicetak!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak PDF:\n" + e.getMessage());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Order::new);
    }
}