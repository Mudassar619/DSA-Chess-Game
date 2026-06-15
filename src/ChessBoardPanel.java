import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChessBoardPanel extends JPanel {
    private final MainMenu frame;
    private final ChessGame game;
    private final ScoreManager scoreManager;
    private final JButton[][] cells = new JButton[8][8];
    private final DefaultListModel<String> moveModel = new DefaultListModel<String>();
    private final JLabel turnLabel = new JLabel();
    private final JLabel aiLabel = new JLabel();
    private final JTextArea capturedArea = new JTextArea();
    private final JTextArea actionStepsArea = new JTextArea();
    private int selectedRow = -1, selectedCol = -1;
    private List<int[]> validMoves;
    private boolean computerThinking = false;
    private boolean finishRecorded = false;

    private final Color light = new Color(232, 226, 210);
    private final Color dark = new Color(176, 125, 75);
    private final Color selected = new Color(244, 183, 92);
    // Soft brown/gold move indicators to match the light-brown chess theme.
    private final Color legalDot = new Color(125, 82, 45);
    private final Color legalDotBorder = new Color(248, 219, 157);
    private final Color captureRing = new Color(111, 58, 28);
    private final Color captureFill = new Color(248, 219, 157, 90);

    private class BoardButton extends JButton {
        boolean selectedCell = false;
        boolean legalEmptyCell = false;
        boolean legalCaptureCell = false;

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            if (selectedCell) {
                g2.setStroke(new BasicStroke(4));
                g2.setColor(selected);
                g2.drawRoundRect(4, 4, getWidth() - 9, getHeight() - 9, 10, 10);
            }
            if (legalEmptyCell) {
                int size = Math.max(14, Math.min(getWidth(), getHeight()) / 4);
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.setColor(legalDotBorder);
                g2.fillOval(x - 3, y - 3, size + 6, size + 6);
                g2.setColor(legalDot);
                g2.fillOval(x, y, size, size);
            }
            if (legalCaptureCell) {
                g2.setColor(captureFill);
                g2.fillOval(7, 7, getWidth() - 14, getHeight() - 14);
                g2.setStroke(new BasicStroke(5));
                g2.setColor(captureRing);
                g2.drawOval(8, 8, getWidth() - 16, getHeight() - 16);
                g2.setStroke(new BasicStroke(2));
                g2.setColor(legalDotBorder);
                g2.drawOval(15, 15, getWidth() - 30, getHeight() - 30);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public ChessBoardPanel(MainMenu frame, ChessGame game, ScoreManager scoreManager) {
        this.frame = frame;
        this.game = game;
        this.scoreManager = scoreManager;
        setLayout(new BorderLayout(14, 14));
        setBackground(new Color(68, 43, 26));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        add(topBar(), BorderLayout.NORTH);
        add(boardPanel(), BorderLayout.CENTER);
        add(sidePanel(), BorderLayout.EAST);
        refresh();
        maybeComputerMove();
    }

    private JPanel topBar() {
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        turnLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        turnLabel.setForeground(Color.WHITE);
        aiLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        aiLabel.setForeground(new Color(230, 190, 135));
        top.add(turnLabel, BorderLayout.WEST);
        top.add(aiLabel, BorderLayout.EAST);
        return top;
    }

    private JPanel boardPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 155, 95), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JPanel board = new JPanel(new GridLayout(8, 8, 2, 2));
        board.setOpaque(false);
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton b = new BoardButton();
                b.setFont(new Font("Serif", Font.BOLD, 46));
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setOpaque(false);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                int rr = r, cc = c;
                b.addActionListener(e -> handleClick(rr, cc));
                cells[r][c] = b;
                board.add(b);
            }
        }
        wrap.add(board, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel sidePanel() {
        JPanel side = new JPanel(new BorderLayout(10, 10));
        side.setPreferredSize(new Dimension(370, 0));
        side.setOpaque(false);

        JList<String> moves = new JList<String>(moveModel);
        moves.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        moves.setBackground(new Color(250, 241, 225));
        JScrollPane moveScroll = new JScrollPane(moves);
        moveScroll.setBorder(BorderFactory.createTitledBorder("Move History"));

        actionStepsArea.setEditable(false);
        actionStepsArea.setLineWrap(true);
        actionStepsArea.setWrapStyleWord(true);
        actionStepsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        actionStepsArea.setBackground(new Color(250, 241, 225));
        actionStepsArea.setText("Click a pawn to show its respected steps here.");
        JScrollPane actionScroll = new JScrollPane(actionStepsArea);
        actionScroll.setBorder(BorderFactory.createTitledBorder("Pawn Steps"));

        JPanel buttons = new JPanel(new GridLayout(0, 1, 8, 8));
        buttons.setOpaque(false);
        JButton undo = frame.makeButton("Undo Move");
        JButton aiMove = frame.makeButton("Computer Hint/Move");
        JButton save = frame.makeButton("Save Game");
        JButton restart = frame.makeButton("Restart");
        JButton export = frame.makeButton("Export Report");
        JButton back = frame.makeButton("Back to Menu");
        buttons.add(undo); buttons.add(aiMove); buttons.add(save); buttons.add(restart); buttons.add(export); buttons.add(back);

        undo.addActionListener(e -> {
            if (!computerThinking) {
                game.undo();
                // In Human vs Computer mode, undo should bring the human back to their previous turn,
                // not immediately let the computer replay another move.
                if (!game.getUndoStack().isEmpty() && game.isComputerTurn()) {
                    game.undo();
                }
                clearSelection();
                refresh();
            }
        });
        aiMove.addActionListener(e -> forceComputerOrHint());
        save.addActionListener(e -> saveGame());
        restart.addActionListener(e -> { game.reset(); clearSelection(); finishRecorded = false; refresh(); maybeComputerMove(); });
        export.addActionListener(e -> exportReport());
        back.addActionListener(e -> frame.showMenu());

        JPanel upper = new JPanel(new GridLayout(2, 1, 8, 8));
        upper.setOpaque(false);
        upper.add(moveScroll);
        upper.add(actionScroll);
        side.add(upper, BorderLayout.CENTER);
        side.add(buttons, BorderLayout.SOUTH);
        return side;
    }

    private void handleClick(int r, int c) {
        if (computerThinking || game.isComputerTurn()) return;
        if (selectedRow == -1) {
            selectPiece(r, c);
            return;
        }

        if (selectedRow == r && selectedCol == c) {
            clearSelection();
            refresh();
            return;
        }

        ChessPiece clicked = game.getPiece(r, c);
        if (clicked != null && clicked.getColor() == game.getCurrentTurn()) {
            selectPiece(r, c);
            return;
        }

        String result = game.move(selectedRow, selectedCol, r, c);
        if (!"OK".equals(result)) JOptionPane.showMessageDialog(this, result);
        clearSelection();
        refresh();
        if (game.isGameOver()) finishGame(); else maybeComputerMove();
    }

    private void selectPiece(int r, int c) {
        ChessPiece p = game.getPiece(r, c);
        if (p == null) return;
        if (p.getColor() != game.getCurrentTurn()) {
            JOptionPane.showMessageDialog(this, "You can only move your own piece.");
            return;
        }
        selectedRow = r; selectedCol = c;
        validMoves = game.getValidMoves(r, c);
        updatePawnActionSteps(p, r, c);
        updateBoardVisuals();
    }

    private void maybeComputerMove() {
        if (game.isGameOver() || !game.isComputerTurn() || computerThinking) return;
        computerThinking = true;
        aiLabel.setText("Computer thinking with " + game.getComputerDifficulty() + " AI...");
        Timer t = new Timer(180, e -> {
            ((Timer)e.getSource()).stop();
            String result = game.makeComputerMove();
            computerThinking = false;
            clearSelection();
            refresh();
            if (!"OK".equals(result)) JOptionPane.showMessageDialog(this, result);
            if (game.isGameOver()) finishGame(); else maybeComputerMove();
        });
        t.setRepeats(false);
        t.start();
    }

    private void forceComputerOrHint() {
        if (computerThinking || game.isGameOver()) return;
        if (game.isComputerTurn()) { maybeComputerMove(); return; }
        ComputerPlayer.AIMove hint = ComputerPlayer.chooseMove(game, game.getCurrentTurn(), game.getComputerDifficulty());
        if (hint == null) JOptionPane.showMessageDialog(this, "No legal move found.");
        else JOptionPane.showMessageDialog(this, "Best AI suggestion: " + hint.notation() + "\nScore: " + hint.score);
    }

    private void finishGame() {
        if (finishRecorded) return;
        finishRecorded = true;
        scoreManager.recordMatch(game.getWhitePlayer(), game.getBlackPlayer(), game.getWinner());
        FileManager.appendMatch(game.getWhitePlayer(), game.getBlackPlayer(), game.getWinner(), game.getMoveHistory().size());
        JOptionPane.showMessageDialog(this, "Game Over! Winner: " + game.getWinner());
    }

    private void saveGame() {
        try {
            FileManager.saveGame(game);
            JOptionPane.showMessageDialog(this, "Game saved in saved_game.txt");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage()); }
    }

    private void exportReport() {
        try {
            FileManager.exportMatchReport(game);
            JOptionPane.showMessageDialog(this, "Report exported as match_report.txt");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage()); }
    }

    private void clearSelection() {
        selectedRow = selectedCol = -1;
        validMoves = null;
        clearActionSteps();
    }


    private void updatePawnActionSteps(ChessPiece p, int r, int c) {
        if (p == null || p.getType() != ChessPiece.Type.PAWN) {
            clearActionSteps();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Pawn selected\n");
        sb.append("Square: ").append(squareName(r, c)).append("\n\n");
        sb.append("Respected steps/rules:\n");
        sb.append("1. One step forward if the next square is empty.\n");
        sb.append("2. Two steps forward only from the starting row if both squares are empty.\n");
        sb.append("3. Diagonal move is allowed only when capturing an opponent piece.\n");
        sb.append("4. En passant is allowed only immediately after the opponent pawn moves two squares.\n");
        sb.append("5. Promotion happens when the pawn reaches the last row.\n\n");
        sb.append("Available moves now:\n");
        if (validMoves == null || validMoves.isEmpty()) {
            sb.append("No legal pawn move available from this square.");
        } else {
            int i = 1;
            for (int[] move : validMoves) {
                sb.append(i++).append(". ").append(squareName(r, c)).append(" -> ").append(squareName(move[0], move[1]));
                ChessPiece target = game.getPiece(move[0], move[1]);
                if (target != null) sb.append(" (capture ").append(target.symbol()).append(")");
                else if (Math.abs(move[1] - c) == 1) sb.append(" (en passant/capture square)");
                else sb.append(" (move)");
                sb.append("\n");
            }
        }
        actionStepsArea.setText(sb.toString());
        actionStepsArea.setCaretPosition(0);
    }

    private String squareName(int r, int c) {
        char file = (char) ('a' + c);
        int rank = 8 - r;
        return "" + file + rank;
    }

    private void clearActionSteps() {
        actionStepsArea.setText("Click a pawn to show its respected steps here.");
    }

    private void updateBoardVisuals() {
        List<int[]> activeMoves = selectedRow != -1 ? validMoves : null;
        int activeRow = selectedRow;
        int activeCol = selectedCol;
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) {
            JButton b = cells[r][c];
            ChessPiece p = game.getPiece(r, c);
            b.setText(p == null ? "" : p.symbol());
            b.setForeground(p != null && p.getColor() == ChessPiece.Color.WHITE ? Color.WHITE : new Color(30, 30, 40));
            b.setBackground((r + c) % 2 == 0 ? light : dark);
            if (b instanceof BoardButton) {
                BoardButton bb = (BoardButton) b;
                bb.selectedCell = activeRow == r && activeCol == c;
                bb.legalEmptyCell = false;
                bb.legalCaptureCell = false;
            }
            if (activeMoves != null) {
                for (int[] m : activeMoves) {
                    if (m[0] == r && m[1] == c && b instanceof BoardButton) {
                        BoardButton bb = (BoardButton) b;
                        if (game.getPiece(r, c) == null) {
                            boolean enPassantSquare = false;
                            ChessPiece activePiece = game.getPiece(activeRow, activeCol);
                            if (activePiece != null && activePiece.getType() == ChessPiece.Type.PAWN && Math.abs(c - activeCol) == 1) {
                                enPassantSquare = true;
                            }
                            bb.legalEmptyCell = !enPassantSquare;
                            bb.legalCaptureCell = enPassantSquare;
                        } else bb.legalCaptureCell = true;
                    }
                }
            }
            b.repaint();
        }
    }

    private void refresh() {
        turnLabel.setText("Turn: " + game.getCurrentPlayerName() + " / " + game.getCurrentTurn()
                + (game.isKingInCheck(game.getCurrentTurn()) ? "  - KING IN CHECK" : "")
                + "  |  " + game.getGameStatus());
        aiLabel.setText(game.isComputerTurn() ? "Computer Mode: " + game.getComputerDifficulty() : "Human Turn");
        updateBoardVisuals();
        moveModel.clear();
        for (String m : game.getMoveHistory().asTextList()) moveModel.addElement(m);
    }
}
