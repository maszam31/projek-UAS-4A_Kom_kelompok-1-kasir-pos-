package form;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import Database.DatabaseConnection;

public class Login extends JFrame {
    private JTextField txtUssername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnCancel;
    private JCheckBox showPassword;

    public Login() {
        setTitle("Login Kasir");
        setSize(350, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel background
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bg = new ImageIcon(getClass().getResource("/images/background_login.jpg"));
                g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        ImageIcon logoIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("/images/logo1.png"))
                        .getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH)
        );
        JLabel logoLabel = new JLabel(logoIcon);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(logoLabel, gbc);

        // Username Label & Icon
        ImageIcon userIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("/images/ussername.jpg"))
                        .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
        );
        JLabel lblUsername = new JLabel(" Username", userIcon, JLabel.LEFT);
        lblUsername.setForeground(Color.WHITE);
        lblUsername.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(lblUsername, gbc);

        // Username Field
        txtUssername = new JTextField();
        txtUssername.setPreferredSize(new Dimension(220, 28));
        txtUssername.setFont(new Font("Arial", Font.PLAIN, 13));
        gbc.gridy = 2;
        panel.add(txtUssername, gbc);

        // Password Label & Icon
        ImageIcon passIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("/images/password.jpg"))
                        .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
        );
        JLabel lblPassword = new JLabel(" Password", passIcon, JLabel.LEFT);
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridy = 3;
        panel.add(lblPassword, gbc);

        // Password Field
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(220, 28));
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 13));
        gbc.gridy = 4;
        panel.add(txtPassword, gbc);

        // Show password checkbox
        showPassword = new JCheckBox("Lihat Password");
        showPassword.setOpaque(false);
        showPassword.setForeground(Color.WHITE);
        showPassword.setFont(new Font("Arial", Font.PLAIN, 11));
        showPassword.addActionListener(e -> {
            if (showPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
        gbc.gridy = 5;
        panel.add(showPassword, gbc);

        // Tombol Login dan Batal
        btnLogin = new JButton("Login");
        btnCancel = new JButton("Batal");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnCancel);
        gbc.gridy = 6;
        panel.add(buttonPanel, gbc);

        // Label Register (di tengah)
        JLabel lblRegister = new JLabel("<HTML><U>Belum punya akun? Daftar di sini</U></HTML>");
        lblRegister.setForeground(Color.CYAN);
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegister.setFont(new Font("Arial", Font.PLAIN, 12));
        lblRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose(); // Tutup form login
                new Register().setVisible(true); // Buka form register
            }
        });
        gbc.gridy = 7;
        gbc.insets = new Insets(20, 10, 10, 10);
        panel.add(lblRegister, gbc);

        add(panel);

        // Event Login
        btnLogin.addActionListener(e -> login());
        txtPassword.addActionListener(e -> login());
        txtUssername.addActionListener(e -> login());

        btnCancel.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin mau keluar?", "Keluar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void login() {
        String username = txtUssername.getText().trim();
        String password = String.valueOf(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi semua jangan kosong nanti dikepret!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM user WHERE ussername=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login berhasil!");
                new MainMenu().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Ussername atau Password salah!");
                txtUssername.setText("");
                txtPassword.setText("");
                txtUssername.requestFocus();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error koneksi: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}