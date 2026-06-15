import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MatchHistoryNetworkPanel extends JPanel {
    private final MainMenu frame;
    private final NetworkCanvas canvas;

    public MatchHistoryNetworkPanel(MainMenu frame) {
        this.frame = frame;
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(68, 43, 26));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("Match History Network", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        canvas = new NetworkCanvas(FileManager.readMatchHistory());
        canvas.setBackground(new Color(250, 241, 225));
        JScrollPane scroll = new JScrollPane(canvas);
        scroll.setBorder(BorderFactory.createTitledBorder("Graph DSA: players as vertices, matches as edges"));
        add(scroll, BorderLayout.CENTER);

        JTextArea notes = new JTextArea("This graph uses the saved match history file. " +
                "Every unique player is a vertex. Every match between two players is an edge. " +
                "The edge label shows how many games were played between those two players.");
        notes.setEditable(false);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notes.setBackground(new Color(250, 241, 225));
        JScrollPane noteScroll = new JScrollPane(notes);
        noteScroll.setPreferredSize(new Dimension(0, 80));

        JButton refresh = frame.makeButton("Refresh Network");
        JButton back = frame.makeButton("Back to Menu");
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        buttons.setOpaque(false);
        buttons.add(refresh);
        buttons.add(back);
        bottom.add(noteScroll, BorderLayout.CENTER);
        bottom.add(buttons, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        refresh.addActionListener(e -> canvas.setRows(FileManager.readMatchHistory()));
        back.addActionListener(e -> frame.showMenu());
    }

    static class NetworkCanvas extends JPanel {
        private List<String[]> rows;
        private final Map<String, Point> nodePositions = new LinkedHashMap<String, Point>();
        private final Map<String, Integer> edgeCounts = new LinkedHashMap<String, Integer>();
        private final Map<String, Integer> winCounts = new HashMap<String, Integer>();

        NetworkCanvas(List<String[]> rows) {
            setPreferredSize(new Dimension(900, 560));
            setRows(rows);
        }

        void setRows(List<String[]> rows) {
            this.rows = rows == null ? new ArrayList<String[]>() : rows;
            buildGraph();
            repaint();
        }

        private void buildGraph() {
            nodePositions.clear();
            edgeCounts.clear();
            winCounts.clear();
            for (String[] row : rows) {
                if (row.length < 5) continue;
                String white = clean(row[0]);
                String black = clean(row[1]);
                String winner = clean(row[2]);
                if (white.length() == 0 || black.length() == 0) continue;
                nodePositions.put(white, new Point());
                nodePositions.put(black, new Point());
                String edgeKey = edgeKey(white, black);
                edgeCounts.put(edgeKey, edgeCounts.containsKey(edgeKey) ? edgeCounts.get(edgeKey) + 1 : 1);
                if (winner.length() > 0 && !winner.equalsIgnoreCase("Draw")) {
                    winCounts.put(winner, winCounts.containsKey(winner) ? winCounts.get(winner) + 1 : 1);
                }
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (nodePositions.isEmpty()) {
                g2.setColor(new Color(75, 48, 28));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                drawCentered(g2, "No match history found yet.", w / 2, h / 2 - 15);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                drawCentered(g2, "Finish a game first, then return to this panel to see the network graph.", w / 2, h / 2 + 20);
                g2.dispose();
                return;
            }

            layoutNodes(w, h);
            g2.setStroke(new BasicStroke(3f));
            for (Map.Entry<String, Integer> edge : edgeCounts.entrySet()) {
                String[] players = edge.getKey().split("\\|", 2);
                Point a = nodePositions.get(players[0]);
                Point b = nodePositions.get(players[1]);
                if (a == null || b == null) continue;
                g2.setColor(new Color(125, 82, 45));
                g2.drawLine(a.x, a.y, b.x, b.y);
                g2.setColor(new Color(80, 48, 25));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                drawCentered(g2, edge.getValue() + " game(s)", (a.x + b.x) / 2, (a.y + b.y) / 2 - 6);
            }

            for (Map.Entry<String, Point> node : nodePositions.entrySet()) {
                String name = node.getKey();
                Point p = node.getValue();
                int radius = 40;
                int wins = winCounts.containsKey(name) ? winCounts.get(name) : 0;
                g2.setColor(new Color(210, 155, 95));
                g2.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
                g2.setColor(new Color(85, 50, 24));
                g2.setStroke(new BasicStroke(4f));
                g2.drawOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(Color.WHITE);
                drawCentered(g2, shorten(name), p.x, p.y - 4);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                drawCentered(g2, "wins: " + wins, p.x, p.y + 15);
            }
            g2.dispose();
        }

        private void layoutNodes(int w, int h) {
            int n = nodePositions.size();
            int cx = w / 2;
            int cy = h / 2;
            int radius = Math.max(150, Math.min(w, h) / 2 - 85);
            int i = 0;
            for (String name : nodePositions.keySet()) {
                double angle = -Math.PI / 2 + (2 * Math.PI * i / Math.max(1, n));
                nodePositions.put(name, new Point(cx + (int) (Math.cos(angle) * radius), cy + (int) (Math.sin(angle) * radius)));
                i++;
            }
        }

        private String clean(String value) {
            return value == null ? "" : value.trim();
        }

        private String edgeKey(String a, String b) {
            return a.compareToIgnoreCase(b) <= 0 ? a + "|" + b : b + "|" + a;
        }

        private String shorten(String value) {
            return value.length() <= 14 ? value : value.substring(0, 12) + "...";
        }

        private void drawCentered(Graphics2D g2, String text, int x, int y) {
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, x - fm.stringWidth(text) / 2, y);
        }
    }
}
