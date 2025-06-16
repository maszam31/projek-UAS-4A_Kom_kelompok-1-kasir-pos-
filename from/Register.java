package form;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import Database.DatabaseConnection;

public class Register extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword, txtConfirm;
    private JButton btnRegister, btnBack;
    private JCheckBox chkShowPassword, chkShowConfirm;

    public Register() {
        setTitle("Register Akun");
        setSize(350, 500); // ukuran layar HP
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Panel utama dengan background
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bg = new ImageIcon(getClass().getResource("/images/background_login.jpg"));
                g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Judul
        JLabel lblTitle = new JLabel("DAFTAR AKUN");
        lblTitle.setFont(new Font("Algerian", Font.BOLD, 22));
        lblTitle.setForeground(Color.CYAN);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        // Username
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setForeground(Color.WHITE);
        lblUsername.setFont(lblUsername.getFont().deriveFont(Font.BOLD));
        panel.add(lblUsername, gbc);

        gbc.gridx = 1;
        txtUsername = new JTextField(15);
        panel.add(txtUsername, gbc);

        // Password
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setFont(lblPassword.getFont().deriveFont(Font.BOLD));
        panel.add(lblPassword, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        txtPassword.setEchoChar('•');
        panel.add(txtPassword, gbc);

        // Checkbox show password
        gbc.gridy++;
        chkShowPassword = new JCheckBox("Tampilkan Password");
        chkShowPassword.setOpaque(false);
        chkShowPassword.setForeground(Color.WHITE);
        chkShowPassword.addItemListener(e -> {
            txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : '•');
        });
        gbc.gridx = 1;
        panel.add(chkShowPassword, gbc);

        // Konfirmasi Password
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblConfirm = new JLabel("Konfirmasi:");
        lblConfirm.setForeground(Color.WHITE);
        lblConfirm.setFont(lblConfirm.getFont().deriveFont(Font.BOLD));
        panel.add(lblConfirm, gbc);

        gbc.gridx = 1;
        txtConfirm = new JPasswordField(15);
        txtConfirm.setEchoChar('•');
        panel.add(txtConfirm, gbc);

        // Checkbox show confirm password
        gbc.gridy++;
        chkShowConfirm = new JCheckBox("Tampilkan Konfirmasi");
        chkShowConfirm.setOpaque(false);
        chkShowConfirm.setForeground(Color.WHITE);
        chkShowConfirm.addItemListener(e -> {
            txtConfirm.setEchoChar(chkShowConfirm.isSelected() ? (char) 0 : '•');
        });
        gbc.gridx = 1;
        panel.add(chkShowConfirm, gbc);

        // Tombol Register
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        btnRegister = new JButton("Daftar");
        panel.add(btnRegister, gbc);

        // Tombol kembali
        gbc.gridy++;
        btnBack = new JButton("Kembali ke Login");
        panel.add(btnBack, gbc);

        // Action tombol daftar
        btnRegister.addActionListener(e -> register());

        // Action tombol kembali
        btnBack.addActionListener(e -> {
            dispose();
            new Login().setVisible(true);
        });

        add(panel);
        setVisible(true);
    }

    private void register() {
        String username = txtUsername.getText().trim();
        String password = String.valueOf(txtPassword.getPassword()).trim();
        String confirm = String.valueOf(txtConfirm.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua kolom harus diisi!");
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Password dan konfirmasi harus sama!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Cek apakah username sudah ada
            String cek = "SELECT * FROM user WHERE ussername=?";
            PreparedStatement cekStmt = conn.prepareStatement(cek);
            cekStmt.setString(1, username);
            ResultSet rs = cekStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username sudah digunakan!");
                return;
            }

            // Simpan user baru
            String insert = "INSERT INTO user (ussername, password) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insert);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Berhasil daftar! Silakan login.");
            dispose();
            new Login().setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal daftar: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Register::new);
    }
}