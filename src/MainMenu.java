import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);
    private final ChessGame game = new ChessGame();
    private final ScoreManager scoreManager = new ScoreManager();

    public MainMenu() {
        setTitle("DSA Chess Game");
        setSize(1220, 820);
        setMinimumSize(new Dimension(1120, 760));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        root.add(menuPanel(), "menu");
        root.add(new PlayerSetupPanel(this, game), "setup");
        root.add(new ScoreboardPanel(this, scoreManager), "scoreboard");
        root.add(new MatchHistoryPanel(this), "history");
        root.add(new MinimaxGameTreePanel(this, game), "minimaxTree");
        root.add(new MatchHistoryNetworkPanel(this), "historyNetwork");
        root.add(new InstructionsPanel(this), "instructions");
        add(root);
        showMenu();
    }

    private JPanel menuPanel() {
        AnimatedMenuPanel panel = new AnimatedMenuPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 12, 9, 12);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(28, 44, 32, 44));

        JLabel title = new JLabel("DSA Chess Game", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 56));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0; card.add(title, gbc);

        String[] buttons = {"Start New Game", "Load Saved Game", "View Scoreboard", "Match History", "Minimax Game Tree", "Match History Network", "Instructions", "Exit"};
        for (int i = 0; i < buttons.length; i++) {
            JButton btn = makeButton(buttons[i]);
            gbc.gridy = i + 1;
            card.add(btn, gbc);
            if (buttons[i].equals("Start New Game")) btn.addActionListener(e -> cardLayout.show(root, "setup"));
            else if (buttons[i].equals("Load Saved Game")) btn.addActionListener(e -> loadGame());
            else if (buttons[i].equals("View Scoreboard")) btn.addActionListener(e -> { root.add(new ScoreboardPanel(this, scoreManager), "scoreboard"); cardLayout.show(root, "scoreboard"); });
            else if (buttons[i].equals("Match History")) btn.addActionListener(e -> { root.add(new MatchHistoryPanel(this), "history"); cardLayout.show(root, "history"); });
            else if (buttons[i].equals("Minimax Game Tree")) btn.addActionListener(e -> { root.add(new MinimaxGameTreePanel(this, game), "minimaxTree"); cardLayout.show(root, "minimaxTree"); });
            else if (buttons[i].equals("Match History Network")) btn.addActionListener(e -> { root.add(new MatchHistoryNetworkPanel(this), "historyNetwork"); cardLayout.show(root, "historyNetwork"); });
            else if (buttons[i].equals("Instructions")) btn.addActionListener(e -> cardLayout.show(root, "instructions"));
            else if (buttons[i].equals("Exit")) btn.addActionListener(e -> System.exit(0));
        }
        panel.add(card);
        return panel;
    }

    public JButton makeButton(String text) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ButtonModel m = getModel();
                GradientPaint gp = new GradientPaint(0, 0,
                        m.isRollover() ? new Color(210, 155, 95) : new Color(150, 95, 55),
                        getWidth(), getHeight(),
                        m.isRollover() ? new Color(185, 120, 70) : new Color(120, 72, 38));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                if (m.isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 60));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 18));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(340, 54));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setFont(new Font("Segoe UI", Font.BOLD, 19)); }
            public void mouseExited(MouseEvent e) { b.setFont(new Font("Segoe UI", Font.BOLD, 18)); }
        });
        return b;
    }

    public void startGame(String white, String black) {
        startGame(white, black, false, false, ComputerPlayer.Difficulty.MEDIUM);
    }

    public void startGame(String white, String black, boolean whiteComputer, boolean blackComputer, ComputerPlayer.Difficulty difficulty) {
        game.reset();
        game.setPlayers(white, black);
        game.setComputerSettings(whiteComputer, blackComputer, difficulty);
        root.add(new ChessBoardPanel(this, game, scoreManager), "game");
        cardLayout.show(root, "game");
    }

    private void loadGame() {
        try {
            if (FileManager.loadGame(game)) {
                game.setComputerSettings(false, false, ComputerPlayer.Difficulty.MEDIUM);
                root.add(new ChessBoardPanel(this, game, scoreManager), "game");
                cardLayout.show(root, "game");
            } else JOptionPane.showMessageDialog(this, "No saved_game.txt found.");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Could not load game: " + ex.getMessage()); }
    }

    public void showMenu() { cardLayout.show(root, "menu"); }

    static class AnimatedMenuPanel extends JPanel implements ActionListener {
        private int tick = 0;
        private final Timer timer = new Timer(45, this);
        AnimatedMenuPanel() { timer.start(); }
        public void actionPerformed(ActionEvent e) { tick++; repaint(); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(55, 35, 22), getWidth(), getHeight(), new Color(130, 82, 45)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 18));
            for (int i = 0; i < 28; i++) {
                int x = (i * 97 + tick * (2 + i % 4)) % Math.max(getWidth(), 1);
                int y = (i * 53 + (int)(40 * Math.sin((tick + i) * 0.08))) % Math.max(getHeight(), 1);
                g2.fillOval(x, y, 8 + (i % 5) * 4, 8 + (i % 5) * 4);
            }
            String pieces = "♔ ♕ ♖ ♗ ♘ ♙ ♚ ♛ ♜ ♝ ♞ ♟";
            g2.setFont(new Font("Serif", Font.BOLD, 44));
            g2.setColor(new Color(255, 255, 255, 22));
            for (int i = 0; i < 12; i++) {
                int x = (i * 160 + tick * 3) % (getWidth() + 200) - 100;
                int y = 95 + (i * 55) % Math.max(getHeight() - 130, 1);
                g2.drawString(pieces.substring(i * 2, i * 2 + 1), x, y);
            }
            g2.dispose();
        }
    }
}
