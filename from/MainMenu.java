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
                ImageIcon bg = new ImageIcon(getClass().getResource("/images/background_menu.jpg"));
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

        // Tombol Menu → buka MenuMakanan
        panelIsi.add(buatPanelMenu("Menu", "/images/makanan.jpg", e -> new MenuMakanan()));

        // Tombol Transaksi → buka form Transaksi
        panelIsi.add(buatPanelMenu("Transaksi", "/images/trans.jpg", e -> new Transaksi()));

        panelUtama.add(panelIsi, BorderLayout.CENTER);

        // Panel bawah (tombol Back)
        JPanel panelBottom = new JPanel(new BorderLayout());
        panelBottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        panelBottom.setOpaque(false);
        JButton btnBack = new JButton("Back");
        panelBottom.add(btnBack, BorderLayout.EAST);
        panelUtama.add(panelBottom, BorderLayout.SOUTH);

        // Event tombol Back
        btnBack.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Kembali ke halaman sebelumnya");
        });

        setContentPane(panelUtama);
        setVisible(true);
    }

    /**
     * @param nama       teks tombol
     * @param pathIcon   path resource icon
     * @param listener   ActionListener yang dijalankan saat tombol diklik
     */
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