import javax.swing.*;
import java.awt.*;

public class PlayerSetupPanel extends JPanel {
    public PlayerSetupPanel(MainMenu frame, ChessGame game) {
        setLayout(new GridBagLayout());
        setBackground(new Color(68, 43, 26));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 150, 95), 1),
                BorderFactory.createEmptyBorder(28, 42, 28, 42)));

        JLabel title = new JLabel("Game Setup", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Select player type, AI difficulty, and match mode", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(242, 222, 195));

        JTextField white = field("White player name");
        JTextField black = field("Black player name");
        JComboBox<String> mode = combo(new String[]{"Human vs Human", "Human vs Computer", "Computer vs Human"});
        JComboBox<String> difficulty = combo(new String[]{"EASY", "MEDIUM", "HARD"});

        JButton start = frame.makeButton("Start Game");
        JButton back = frame.makeButton("Back to Menu");

        gbc.gridx = 0; gbc.gridy = 0; card.add(title, gbc);
        gbc.gridy = 1; card.add(subtitle, gbc);
        gbc.gridy = 2; card.add(white, gbc);
        gbc.gridy = 3; card.add(black, gbc);
        gbc.gridy = 4; card.add(labeled("Game Mode", mode), gbc);
        gbc.gridy = 5; card.add(labeled("Computer Difficulty", difficulty), gbc);
        gbc.gridy = 6; card.add(start, gbc);
        gbc.gridy = 7; card.add(back, gbc);
        add(card);

        mode.addActionListener(e -> {
            String selected = (String) mode.getSelectedItem();
            boolean computerWhite = selected != null && selected.startsWith("Computer");
            boolean computerBlack = selected != null && selected.endsWith("Computer");

            white.setEnabled(!computerWhite);
            black.setEnabled(!computerBlack);

            if (computerWhite) {
                white.setText("Computer White");
            } else if (white.getText().trim().equals("Computer White")) {
                white.setText("");
            }

            if (computerBlack) {
                black.setText("Computer Black");
            } else if (black.getText().trim().equals("Computer Black")) {
                black.setText("");
            }
        });

        start.addActionListener(e -> {
            String selected = (String) mode.getSelectedItem();
            boolean computerWhite = selected != null && selected.startsWith("Computer");
            boolean computerBlack = selected != null && selected.endsWith("Computer");
            String whiteName = white.getText().trim().isEmpty() ? "White" : white.getText().trim();
            String blackName = black.getText().trim().isEmpty() ? "Black" : black.getText().trim();
            ComputerPlayer.Difficulty d = ComputerPlayer.Difficulty.valueOf((String) difficulty.getSelectedItem());
            frame.startGame(whiteName, blackName, computerWhite, computerBlack, d);
        });
        back.addActionListener(e -> frame.showMenu());
    }

    private JPanel labeled(String label, JComponent component) {
        JPanel p = new JPanel(new BorderLayout(8, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(245, 230, 205));
        p.add(l, BorderLayout.NORTH);
        p.add(component, BorderLayout.CENTER);
        return p;
    }

    private JComboBox<String> combo(String[] values) {
        JComboBox<String> c = new JComboBox<String>(values);
        c.setFont(new Font("Segoe UI", Font.BOLD, 16));
        c.setPreferredSize(new Dimension(360, 48));
        return c;
    }

    private JTextField field(String text) {
        JTextField f = new JTextField();
        f.setBorder(BorderFactory.createTitledBorder(text));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        f.setPreferredSize(new Dimension(360, 58));
        return f;
    }
}
