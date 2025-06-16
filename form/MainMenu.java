package form;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Menu Kasir");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Panel utama dengan background
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

        // Panel atas (logo)
        JPanel panelAtas = new JPanel();
        panelAtas.setLayout(new BoxLayout(panelAtas, BoxLayout.Y_AXIS));
        panelAtas.setOpaque(false);
        panelAtas.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel lblLogo = new JLabel();
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            ImageIcon logo = new ImageIcon(getClass().getResource("/images/logo1.png"));
            Image scaledLogo = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(scaledLogo));
        } catch (Exception e) {
            lblLogo.setText("Logo");
        }

        panelAtas.add(lblLogo);
        panelUtama.add(panelAtas, BorderLayout.NORTH);

        // Panel isi (menu & transaksi)
        JPanel panelIsi = new JPanel(new GridLayout(1, 2, 20, 0));
        panelIsi.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panelIsi.setOpaque(false);

        panelIsi.add(buatPanelMenu("Menu", "/images/makanan.jpg", e -> {
            new MenuMakanan();
            dispose(); // Tutup jendela MainMenu
        }));

        panelIsi.add(buatPanelMenu("Order", "/images/order.jpg", e -> {
            new Order();
            dispose(); // Tutup jendela MainMenu
        }));

        panelIsi.add(buatPanelMenu("Riwayat", "/images/trans.jpg", e -> {
            new RiwayatPembelian();
            dispose(); // Tutup jendela MainMenu
        }));

        panelUtama.add(panelIsi, BorderLayout.CENTER);

        // Panel bawah (back)
        JPanel panelBottom = new JPanel(new BorderLayout());
        panelBottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panelBottom.setOpaque(false);

        JButton btnBack = new JButton(" Back");
        btnBack.setFont(new Font("SansSerif", Font.PLAIN, 12));
        try {
            ImageIcon backIcon = new ImageIcon(getClass().getResource("/images/back.jpeg"));
            Image scaledBack = backIcon.getImage().getScaledInstance(50, 25, Image.SCALE_SMOOTH);
            btnBack.setIcon(new ImageIcon(scaledBack));
        } catch (Exception e) {
            System.out.println("Icon back.jpg tidak ditemukan.");
        }

        btnBack.setFocusPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        btnBack.addActionListener(e -> {
            new Login().setVisible(true);
            dispose();
        });

        panelBottom.add(btnBack, BorderLayout.WEST);
        panelUtama.add(panelBottom, BorderLayout.SOUTH);

        setContentPane(panelUtama);
        setVisible(true);
    }

    private JPanel buatPanelMenu(String nama, String pathIcon, ActionListener listener) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel lblIcon = new JLabel();
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(pathIcon));
            Image scaledIcon = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            lblIcon.setIcon(new ImageIcon(scaledIcon));
        } catch (Exception e) {
            lblIcon.setText("Icon hilang");
        }

        JButton btn = new JButton(nama);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(listener);

        panel.add(lblIcon);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btn);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}