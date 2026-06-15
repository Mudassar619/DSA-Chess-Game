import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ScoreboardPanel extends JPanel {
    public ScoreboardPanel(MainMenu frame, ScoreManager manager) {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(68, 43, 26));
        JLabel title = new JLabel("Scoreboard", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        String[] cols = {"Player", "Wins", "Losses", "Draws", "Total Matches"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (ScoreManager.PlayerRecord r : manager.sortedRecords()) {
            model.addRow(new Object[]{r.name, r.wins, r.losses, r.draws, r.matches});
        }
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(30);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(68, 43, 26));
        JTextField search = new JTextField(18);
        JButton find = frame.makeButton("Search Player");
        JButton back = frame.makeButton("Back to Menu");
        bottom.add(search); bottom.add(find); bottom.add(back);
        add(bottom, BorderLayout.SOUTH);

        find.addActionListener(e -> {
            ScoreManager.PlayerRecord r = manager.search(search.getText().trim());
            if (r == null) JOptionPane.showMessageDialog(this, "Player not found.");
            else JOptionPane.showMessageDialog(this, r.name + "\nWins: " + r.wins + "\nLosses: " + r.losses + "\nDraws: " + r.draws + "\nMatches: " + r.matches);
        });
        back.addActionListener(e -> frame.showMenu());
    }
}
