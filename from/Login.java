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

    public Login() {
        setTitle("Login Kasir");
        setSize(500, 500); // Ukuran lebih besar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel utama dengan background
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
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        ImageIcon logoIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("/images/logo1.png"))
                        .getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)
        );
        JLabel logoLabel = new JLabel(logoIcon);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(logoLabel, gbc);

        // Username
        ImageIcon userIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("/images/ussername.jpg"))
                        .getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)
        );
        JLabel lblUsername = new JLabel(" Ussername", userIcon, JLabel.LEFT);
        lblUsername.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(lblUsername, gbc);

        txtUssername = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtUssername, gbc);

        // Password
        ImageIcon passIcon = new ImageIcon(
                new ImageIcon(getClass().getResource("/images/password.jpg"))
                        .getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)
        );
        JLabel lblPassword = new JLabel(" Password", passIcon, JLabel.LEFT);
        lblPassword.setForeground(Color.WHITE);
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // Tombol Login dan Cancel (Tengah)
        btnLogin = new JButton("Login");
        btnCancel = new JButton("Cancel");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnCancel);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        add(panel);

        // Event listener
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