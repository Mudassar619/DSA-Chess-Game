import javax.swing.*;
import java.awt.*;

public class InstructionsPanel extends JPanel {
    public InstructionsPanel(MainMenu frame) {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(68, 43, 26));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("Chess Rules & Concepts", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(new Color(255, 238, 210));
        add(title, BorderLayout.NORTH);

        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setMargin(new Insets(14, 16, 14, 16));
        text.setBackground(new Color(250, 241, 225));
        text.setForeground(new Color(45, 30, 18));
        text.setText(
                "The Chess Pieces & Their Movements\n\n" +
                "Each type of piece moves and captures differently:\n\n" +
                "King: Moves exactly one square in any direction: horizontal, vertical, or diagonal.\n\n" +
                "Queen: Combines the moves of the rook and bishop. It moves any number of empty squares in any direction.\n\n" +
                "Rook: Moves any number of empty squares horizontally or vertically.\n\n" +
                "Bishop: Moves any number of empty squares diagonally.\n\n" +
                "Knight: Moves in an L shape: two squares in one cardinal direction, then one square perpendicular. It is the only piece that can jump over others.\n\n" +
                "Pawn: Moves one square straight forward, or two squares on its initial move. It captures opponent pieces exactly one square diagonally forward.\n\n" +
                "Special Rules\n\n" +
                "Castling: A defensive move combining the King and Rook in one turn. The King moves two squares toward a Rook, and the Rook moves to the square directly next to the King. This can only happen if neither piece has moved yet, there are no pieces between them, and the King is not in check.\n\n" +
                "Pawn Promotion: If a pawn reaches the furthest row from its starting position, it turns into another piece except a King. Players usually choose a Queen.\n\n" +
                "En Passant: A special pawn capture. If an opponent's pawn moves two squares forward from its starting position and lands directly beside your pawn, you can capture it diagonally on the very next move, as if it had only moved one square.\n\n" +
                "Winning and Drawing\n\n" +
                "Check & Checkmate: When a King is threatened with capture, it is in check. If it cannot escape check by moving, blocking the attacker, or capturing the attacking piece, it is checkmate and the game is over.\n\n" +
                "Stalemate: The game ends in a draw if the player whose turn it is has no legal moves available and their King is not in check.\n\n" +
                "DSA + AI Used in This Project\n\n" +
                "Stack: Used for undo move history.\n" +
                "Linked List: Used for move history tracking.\n" +
                "Queue: Used for turn management.\n" +
                "Priority Queue: Used for computer move ordering.\n" +
                "HashMap: Used for AI board-state caching.\n" +
                "Minimax + Alpha-Beta: Used for Hard computer mode.\n" +
                "Tree: Used for Visualizing AI Search Process.\n" +
                "Graph: Used for Match History Network Visualization."
        );
        JScrollPane scroll = new JScrollPane(text);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 155, 95), 2));
        add(scroll, BorderLayout.CENTER);

        JButton back = frame.makeButton("Back to Menu");
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
        back.addActionListener(e -> frame.showMenu());
    }
}
