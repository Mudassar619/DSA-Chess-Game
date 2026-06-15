import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MinimaxGameTreePanel extends JPanel {
    private final MainMenu frame;
    private final ChessGame game;
    private final JTree tree;
    private final JComboBox<Integer> depthBox;
    private final JComboBox<Integer> branchBox;
    private final JTextArea notes;

    public MinimaxGameTreePanel(MainMenu frame, ChessGame game) {
        this.frame = frame;
        this.game = game;
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(68, 43, 26));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("Minimax Game Tree", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        depthBox = new JComboBox<Integer>(new Integer[]{1, 2, 3});
        depthBox.setSelectedItem(2);
        branchBox = new JComboBox<Integer>(new Integer[]{2, 3, 4, 5});
        branchBox.setSelectedItem(3);
        JButton refresh = frame.makeButton("Build Tree");
        JButton back = frame.makeButton("Back to Menu");

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        controls.setBackground(new Color(88, 55, 32));
        controls.add(label("Depth"));
        controls.add(depthBox);
        controls.add(label("Moves per level"));
        controls.add(branchBox);
        controls.add(refresh);
        controls.add(back);

        tree = new JTree(buildRoot());
        tree.setFont(new Font("Consolas", Font.PLAIN, 14));
        tree.setRowHeight(24);
        tree.setBackground(new Color(250, 241, 225));
        JScrollPane scroll = new JScrollPane(tree);
        scroll.setBorder(BorderFactory.createTitledBorder("Tree DSA: Minimax search with board evaluation"));

        notes = new JTextArea();
        notes.setEditable(false);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notes.setBackground(new Color(250, 241, 225));
        notes.setText("This panel converts the current chess position into a limited minimax game tree. " +
                "Each parent node represents a board state, and each child node represents one possible move. " +
                "MAX is the current player trying to increase the score; MIN is the opponent trying to reduce it.");
        JScrollPane noteScroll = new JScrollPane(notes);
        noteScroll.setPreferredSize(new Dimension(0, 115));
        noteScroll.setBorder(BorderFactory.createTitledBorder("How it works"));

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(controls, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        center.add(noteScroll, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        refresh.addActionListener(e -> refreshTree());
        back.addActionListener(e -> frame.showMenu());
        expandAll();
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(Color.WHITE);
        return l;
    }

    private void refreshTree() {
        tree.setModel(new DefaultTreeModel(buildRoot()));
        expandAll();
    }

    private DefaultMutableTreeNode buildRoot() {
        ChessPiece.Color rootColor = game.getCurrentTurn();
        int depth = ((Integer) depthBox.getSelectedItem()).intValue();
        int branchLimit = ((Integer) branchBox.getSelectedItem()).intValue();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT: " + rootColor + " to move | " + game.getCurrentPlayerName());
        buildChildren(root, game.copyBoard(), rootColor, rootColor, depth, branchLimit, true);
        if (root.getChildCount() == 0) root.add(new DefaultMutableTreeNode("No legal moves available from this position."));
        return root;
    }

    private int buildChildren(DefaultMutableTreeNode parent, ChessPiece[][] board, ChessPiece.Color turn,
                              ChessPiece.Color maxColor, int depth, int branchLimit, boolean maxLayer) {
        if (depth == 0) {
            int eval = ComputerPlayer.evaluate(board, maxColor);
            parent.add(new DefaultMutableTreeNode("EVAL = " + eval));
            return eval;
        }

        ArrayList<ComputerPlayer.AIMove> moves = game.getAllLegalMovesOnBoard(board, turn);
        if (moves.isEmpty()) {
            int eval = game.isKingInCheckOnBoardCopy(board, turn) ? (turn == maxColor ? -100000 : 100000) : 0;
            parent.add(new DefaultMutableTreeNode("Terminal position | score = " + eval));
            return eval;
        }

        Collections.sort(moves, new Comparator<ComputerPlayer.AIMove>() {
            public int compare(ComputerPlayer.AIMove a, ComputerPlayer.AIMove b) {
                return Integer.compare(b.score, a.score);
            }
        });

        int best = maxLayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int shown = Math.min(branchLimit, moves.size());
        for (int i = 0; i < shown; i++) {
            ComputerPlayer.AIMove move = moves.get(i);
            ChessPiece[][] next = game.copyBoard(board);
            game.applyMoveOnBoard(next, move.fromRow, move.fromCol, move.toRow, move.toCol);
            DefaultMutableTreeNode child = new DefaultMutableTreeNode((maxLayer ? "MAX" : "MIN") + " move " + move.notation() + " | tactical=" + move.score);
            parent.add(child);
            int value = buildChildren(child, next, opposite(turn), maxColor, depth - 1, branchLimit, !maxLayer);
            child.setUserObject(child.getUserObject().toString() + " | minimax=" + value);
            if (maxLayer) best = Math.max(best, value); else best = Math.min(best, value);
        }
        if (moves.size() > shown) parent.add(new DefaultMutableTreeNode("... " + (moves.size() - shown) + " more legal moves hidden to keep the tree readable"));
        return best;
    }

    private ChessPiece.Color opposite(ChessPiece.Color color) {
        return color == ChessPiece.Color.WHITE ? ChessPiece.Color.BLACK : ChessPiece.Color.WHITE;
    }

    private void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
    }
}
