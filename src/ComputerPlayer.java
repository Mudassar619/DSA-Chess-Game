import java.util.*;

public class ComputerPlayer {
    public enum Difficulty { EASY, MEDIUM, HARD }

    public static class AIMove {
        public final int fromRow, fromCol, toRow, toCol;
        public final int score;
        public AIMove(int fr, int fc, int tr, int tc, int score) {
            this.fromRow = fr; this.fromCol = fc; this.toRow = tr; this.toCol = tc; this.score = score;
        }
        public String notation() {
            return "" + (char)('A' + fromCol) + (8 - fromRow) + " → " + (char)('A' + toCol) + (8 - toRow);
        }
    }

    private static final Random RANDOM = new Random();
    private static final HashMap<String, Integer> transpositionTable = new HashMap<String, Integer>();

    public static AIMove chooseMove(ChessGame game, ChessPiece.Color color, Difficulty difficulty) {
        ArrayList<AIMove> moves = game.getAllLegalMoves(color);
        if (moves.isEmpty()) return null;
        if (difficulty == Difficulty.EASY) return moves.get(RANDOM.nextInt(moves.size()));

        // Priority Queue DSA: best tactical moves are explored first.
        PriorityQueue<AIMove> queue = new PriorityQueue<AIMove>(new Comparator<AIMove>() {
            public int compare(AIMove a, AIMove b) { return Integer.compare(b.score, a.score); }
        });
        queue.addAll(moves);

        if (difficulty == Difficulty.MEDIUM) return queue.poll();

        AIMove best = null;
        int bestScore = Integer.MIN_VALUE;
        int depth = 3;
        ArrayList<AIMove> orderedMoves = new ArrayList<AIMove>();
        while (!queue.isEmpty()) orderedMoves.add(queue.poll());

        for (AIMove m : orderedMoves) {
            ChessPiece[][] copy = game.copyBoard();
            game.applyCurrentMoveOnBoard(copy, m.fromRow, m.fromCol, m.toRow, m.toCol);
            int score = minimax(game, copy, opposite(color), color, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (score > bestScore) {
                bestScore = score;
                best = new AIMove(m.fromRow, m.fromCol, m.toRow, m.toCol, score);
            }
        }
        return best == null ? orderedMoves.get(0) : best;
    }

    private static int minimax(ChessGame game, ChessPiece[][] board, ChessPiece.Color turn, ChessPiece.Color aiColor,
                               int depth, int alpha, int beta) {
        String key = boardKey(board, turn, depth);
        Integer cached = transpositionTable.get(key);
        if (cached != null) return cached.intValue();
        if (depth == 0 || !hasKing(board, ChessPiece.Color.WHITE) || !hasKing(board, ChessPiece.Color.BLACK)) {
            int eval = evaluate(board, aiColor);
            transpositionTable.put(key, eval);
            return eval;
        }

        ArrayList<AIMove> moves = game.getAllLegalMovesOnBoard(board, turn);
        if (moves.isEmpty()) {
            int eval;
            if (game.isKingInCheckOnBoardCopy(board, turn)) {
                eval = (turn == aiColor) ? -100000 - depth : 100000 + depth;
            } else {
                eval = 0; // stalemate is a draw
            }
            transpositionTable.put(key, eval);
            return eval;
        }

        boolean maximizing = turn == aiColor;
        int best = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        Collections.sort(moves, new Comparator<AIMove>() {
            public int compare(AIMove a, AIMove b) { return Integer.compare(b.score, a.score); }
        });

        for (AIMove m : moves) {
            ChessPiece[][] next = game.copyBoard(board);
            game.applyMoveOnBoard(next, m.fromRow, m.fromCol, m.toRow, m.toCol);
            int val = minimax(game, next, opposite(turn), aiColor, depth - 1, alpha, beta);
            if (maximizing) {
                best = Math.max(best, val);
                alpha = Math.max(alpha, val);
            } else {
                best = Math.min(best, val);
                beta = Math.min(beta, val);
            }
            if (beta <= alpha) break; // Alpha-Beta Pruning DSA
        }
        transpositionTable.put(key, best);
        return best;
    }

    public static int evaluate(ChessPiece[][] board, ChessPiece.Color aiColor) {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                ChessPiece p = board[r][c];
                if (p == null) continue;
                int value = pieceValue(p.getType());
                // Positional scoring: central squares are stronger.
                int centerBonus = 6 - (Math.abs(3 - r) + Math.abs(3 - c));
                int total = value + centerBonus;
                score += p.getColor() == aiColor ? total : -total;
            }
        }
        return score;
    }

    public static int pieceValue(ChessPiece.Type type) {
        switch (type) {
            case KING: return 20000;
            case QUEEN: return 900;
            case ROOK: return 500;
            case BISHOP: return 330;
            case KNIGHT: return 320;
            case PAWN: return 100;
        }
        return 0;
    }

    private static ChessPiece.Color opposite(ChessPiece.Color color) {
        return color == ChessPiece.Color.WHITE ? ChessPiece.Color.BLACK : ChessPiece.Color.WHITE;
    }

    private static boolean hasKing(ChessPiece[][] board, ChessPiece.Color color) {
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) {
            ChessPiece p = board[r][c];
            if (p != null && p.getColor() == color && p.getType() == ChessPiece.Type.KING) return true;
        }
        return false;
    }

    private static String boardKey(ChessPiece[][] board, ChessPiece.Color turn, int depth) {
        StringBuilder sb = new StringBuilder();
        sb.append(turn).append(depth).append('|');
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) {
            ChessPiece p = board[r][c];
            sb.append(p == null ? "--" : p.shortCode()).append(',');
        }
        return sb.toString();
    }
}
