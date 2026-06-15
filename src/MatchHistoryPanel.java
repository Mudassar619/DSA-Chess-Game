import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MatchHistoryPanel extends JPanel {
    public MatchHistoryPanel(MainMenu frame) {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(68, 43, 26));
        JLabel title = new JLabel("Match History", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"White", "Black", "Winner", "Date/Time", "Moves"}, 0);
        for (String[] row : FileManager.readMatchHistory()) model.addRow(row);
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setRowHeight(30);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton back = frame.makeButton("Back to Menu");
        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(68, 43, 26));
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
        back.addActionListener(e -> frame.showMenu());
    }
}
