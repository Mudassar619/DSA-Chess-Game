import java.util.ArrayList;
import java.util.List;

public class ChessGame {
    private ChessPiece[][] board = new ChessPiece[8][8];
    private ChessPiece.Color currentTurn = ChessPiece.Color.WHITE;
    private final MoveStack undoStack = new MoveStack();
    private final MoveLinkedList moveHistory = new MoveLinkedList();
    private final TurnQueue turnQueue = new TurnQueue();
    private final List<ChessPiece> capturedWhite = new ArrayList<>();
    private final List<ChessPiece> capturedBlack = new ArrayList<>();
    private String whitePlayer = "White";
    private String blackPlayer = "Black";
    private boolean gameOver = false;
    private String winner = "";
    private String gameStatus = "Game in progress";
    private boolean whiteComputer = false;
    private boolean blackComputer = false;
    private ComputerPlayer.Difficulty computerDifficulty = ComputerPlayer.Difficulty.MEDIUM;

    public ChessGame() { reset(); }

    public void setPlayers(String white, String black) {
        this.whitePlayer = white;
        this.blackPlayer = black;
    }

    public void setComputerSettings(boolean whiteComputer, boolean blackComputer, ComputerPlayer.Difficulty difficulty) {
        this.whiteComputer = whiteComputer;
        this.blackComputer = blackComputer;
        this.computerDifficulty = difficulty == null ? ComputerPlayer.Difficulty.MEDIUM : difficulty;
        if (whiteComputer) this.whitePlayer = "Computer White (" + this.computerDifficulty + ")";
        if (blackComputer) this.blackPlayer = "Computer Black (" + this.computerDifficulty + ")";
    }

    public boolean isComputerTurn() { return currentTurn == ChessPiece.Color.WHITE ? whiteComputer : blackComputer; }
    public ComputerPlayer.Difficulty getComputerDifficulty() { return computerDifficulty; }

    public void reset() {
        board = new ChessPiece[8][8];
        currentTurn = ChessPiece.Color.WHITE;
        gameOver = false;
        winner = "";
        gameStatus = "Game in progress";
        undoStack.clear();
        moveHistory.clear();
        capturedWhite.clear();
        capturedBlack.clear();
        turnQueue.clear();
        turnQueue.enqueue(ChessPiece.Color.WHITE);
        setupPieces();
    }

    private void setupPieces() {
        ChessPiece.Type[] order = { ChessPiece.Type.ROOK, ChessPiece.Type.KNIGHT, ChessPiece.Type.BISHOP, ChessPiece.Type.QUEEN,
                ChessPiece.Type.KING, ChessPiece.Type.BISHOP, ChessPiece.Type.KNIGHT, ChessPiece.Type.ROOK };
        for (int c = 0; c < 8; c++) {
            board[0][c] = new ChessPiece(order[c], ChessPiece.Color.BLACK);
            board[1][c] = new ChessPiece(ChessPiece.Type.PAWN, ChessPiece.Color.BLACK);
            board[6][c] = new ChessPiece(ChessPiece.Type.PAWN, ChessPiece.Color.WHITE);
            board[7][c] = new ChessPiece(order[c], ChessPiece.Color.WHITE);
        }
    }

    public ChessPiece getPiece(int r, int c) { return inBounds(r, c) ? board[r][c] : null; }
    public ChessPiece.Color getCurrentTurn() { return currentTurn; }
    public String getCurrentPlayerName() { return currentTurn == ChessPiece.Color.WHITE ? whitePlayer : blackPlayer; }
    public String getWhitePlayer() { return whitePlayer; }
    public String getBlackPlayer() { return blackPlayer; }
    public boolean isGameOver() { return gameOver; }
    public String getWinner() { return winner; }
    public String getGameStatus() { return gameStatus; }
    public MoveStack getUndoStack() { return undoStack; }
    public MoveLinkedList getMoveHistory() { return moveHistory; }
    public List<ChessPiece> getCapturedWhite() { return capturedWhite; }
    public List<ChessPiece> getCapturedBlack() { return capturedBlack; }
    public ChessPiece[][] getBoard() { return board; }
    public boolean inBounds(int r, int c) { return r >= 0 && r < 8 && c >= 0 && c < 8; }

    public List<int[]> getValidMoves(int r, int c) {
        List<int[]> moves = new ArrayList<>();
        ChessPiece p = getPiece(r, c);
        if (p == null || p.getColor() != currentTurn || gameOver) return moves;
        for (int tr = 0; tr < 8; tr++) for (int tc = 0; tc < 8; tc++) {
            if (isLegalMove(r, c, tr, tc)) moves.add(new int[]{tr, tc});
        }
        return moves;
    }

    public String move(int fr, int fc, int tr, int tc) {
        if (gameOver) return "Game already ended.";
        if (!inBounds(fr, fc) || !inBounds(tr, tc)) return "Move outside board.";
        ChessPiece piece = board[fr][fc];
        if (piece == null) return "No piece selected.";
        if (piece.getColor() != currentTurn) return "It is not this piece's turn.";
        if (!isLegalMove(fr, fc, tr, tc)) return "Invalid move: this move is not allowed or leaves the king in check.";

        Move last = getLastMove();
        boolean movedBefore = piece.hasMoved();
        boolean isCastling = piece.getType() == ChessPiece.Type.KING && Math.abs(tc - fc) == 2;
        boolean isEnPassant = isEnPassantMove(board, last, fr, fc, tr, tc);
        int capRow = tr, capCol = tc;
        ChessPiece captured = isEnPassant ? board[fr][tc] : board[tr][tc];
        boolean rookMovedBefore = false;
        int rookFromCol = -1, rookToCol = -1;

        board[tr][tc] = piece;
        board[fr][fc] = null;
        piece.setMoved(true);

        if (isEnPassant) {
            capRow = fr;
            capCol = tc;
            board[capRow][capCol] = null;
        }

        if (isCastling) {
            rookFromCol = tc > fc ? 7 : 0;
            rookToCol = tc > fc ? 5 : 3;
            ChessPiece rook = board[fr][rookFromCol];
            rookMovedBefore = rook != null && rook.hasMoved();
            board[fr][rookToCol] = rook;
            board[fr][rookFromCol] = null;
            if (rook != null) rook.setMoved(true);
        }

        if (captured != null) {
            if (captured.getColor() == ChessPiece.Color.WHITE) capturedWhite.add(captured);
            else capturedBlack.add(captured);
        }

        ChessPiece originalPawn = null;
        boolean promotion = false;
        ChessPiece movePieceForHistory = piece;
        if (piece.getType() == ChessPiece.Type.PAWN && (tr == 0 || tr == 7)) {
            originalPawn = piece;
            ChessPiece queen = new ChessPiece(ChessPiece.Type.QUEEN, piece.getColor());
            queen.setMoved(true);
            board[tr][tc] = queen;
            movePieceForHistory = queen;
            promotion = true;
        }

        Move m = new Move(movePieceForHistory, captured, fr, fc, tr, tc, movedBefore, promotion, originalPawn,
                isCastling, fr, rookFromCol, fr, rookToCol, rookMovedBefore, isEnPassant, capRow, capCol);
        undoStack.push(m);
        moveHistory.add(m);

        switchTurn();
        updateGameStatusAfterMove();
        return "OK";
    }

    public void undo() {
        Move m = undoStack.pop();
        if (m == null) return;
        ChessPiece restorePiece = m.promotion && m.originalPawn != null ? m.originalPawn : m.movedPiece;
        restorePiece.setMoved(m.movedBefore);
        board[m.fromRow][m.fromCol] = restorePiece;
        board[m.toRow][m.toCol] = null;

        if (m.castling) {
            ChessPiece rook = board[m.rookToRow][m.rookToCol];
            board[m.rookFromRow][m.rookFromCol] = rook;
            board[m.rookToRow][m.rookToCol] = null;
            if (rook != null) rook.setMoved(m.rookMovedBefore);
        }

        if (m.enPassant) board[m.capturedRow][m.capturedCol] = m.capturedPiece;
        else board[m.toRow][m.toCol] = m.capturedPiece;

        if (m.capturedPiece != null) {
            if (m.capturedPiece.getColor() == ChessPiece.Color.WHITE && !capturedWhite.isEmpty()) capturedWhite.remove(capturedWhite.size() - 1);
            else if (m.capturedPiece.getColor() == ChessPiece.Color.BLACK && !capturedBlack.isEmpty()) capturedBlack.remove(capturedBlack.size() - 1);
        }
        moveHistory.removeLast();
        gameOver = false;
        winner = "";
        gameStatus = "Move undone";
        currentTurn = restorePiece.getColor();
        rebuildTurnQueue();
    }

    private void switchTurn() { currentTurn = opposite(currentTurn); rebuildTurnQueue(); }
    private void rebuildTurnQueue() { turnQueue.clear(); turnQueue.enqueue(currentTurn); }
    private ChessPiece.Color opposite(ChessPiece.Color c) { return c == ChessPiece.Color.WHITE ? ChessPiece.Color.BLACK : ChessPiece.Color.WHITE; }

    public boolean isLegalMove(int fr, int fc, int tr, int tc) {
        if (!isPseudoLegalMove(board, getLastMove(), fr, fc, tr, tc, true)) return false;
        ChessPiece[][] copy = copyBoard(board);
        applyMoveOnBoard(copy, getLastMove(), fr, fc, tr, tc);
        ChessPiece.Color mover = board[fr][fc].getColor();
        return !isKingInCheckOnBoard(copy, mover);
    }

    private boolean isPseudoLegalMove(ChessPiece[][] b, Move last, int fr, int fc, int tr, int tc, boolean allowCastling) {
        if (!inBounds(fr, fc) || !inBounds(tr, tc) || (fr == tr && fc == tc)) return false;
        ChessPiece p = b[fr][fc];
        if (p == null) return false;
        ChessPiece target = b[tr][tc];
        if (target != null && target.getColor() == p.getColor()) return false;
        // The king is never captured in legal chess. Checkmate ends the game before a king can be taken.
        if (target != null && target.getType() == ChessPiece.Type.KING) return false;

        int dr = tr - fr, dc = tc - fc, adr = Math.abs(dr), adc = Math.abs(dc);
        switch (p.getType()) {
            case PAWN:
                int dir = p.getColor() == ChessPiece.Color.WHITE ? -1 : 1;
                if (dc == 0 && dr == dir && target == null) return true;
                if (dc == 0 && dr == 2 * dir && !p.hasMoved() && isPawnStartRow(p, fr) && target == null && b[fr + dir][fc] == null) return true;
                if (Math.abs(dc) == 1 && dr == dir && target != null && target.getColor() != p.getColor()) return true;
                return isEnPassantMove(b, last, fr, fc, tr, tc);
            case ROOK: return (dr == 0 || dc == 0) && clearPath(b, fr, fc, tr, tc);
            case BISHOP: return adr == adc && clearPath(b, fr, fc, tr, tc);
            case QUEEN: return ((dr == 0 || dc == 0) || adr == adc) && clearPath(b, fr, fc, tr, tc);
            case KNIGHT: return (adr == 2 && adc == 1) || (adr == 1 && adc == 2);
            case KING:
                if (adr <= 1 && adc <= 1) return !isSquareAttacked(b, tr, tc, opposite(p.getColor()));
                return allowCastling && isCastlingMove(b, fr, fc, tr, tc);
        }
        return false;
    }

    private boolean isPawnStartRow(ChessPiece pawn, int row) {
        return pawn.getColor() == ChessPiece.Color.WHITE ? row == 6 : row == 1;
    }

    private boolean isCastlingMove(ChessPiece[][] b, int fr, int fc, int tr, int tc) {
        ChessPiece king = b[fr][fc];
        if (king == null || king.getType() != ChessPiece.Type.KING || king.hasMoved()) return false;
        if (fr != tr || Math.abs(tc - fc) != 2) return false;
        ChessPiece.Color color = king.getColor();
        if (isKingInCheckOnBoard(b, color)) return false;
        int rookCol = tc > fc ? 7 : 0;
        int step = tc > fc ? 1 : -1;
        ChessPiece rook = b[fr][rookCol];
        if (rook == null || rook.getType() != ChessPiece.Type.ROOK || rook.getColor() != color || rook.hasMoved()) return false;
        for (int c = fc + step; c != rookCol; c += step) if (b[fr][c] != null) return false;
        for (int c = fc + step; c != tc + step; c += step) if (isSquareAttacked(b, fr, c, opposite(color))) return false;
        return true;
    }

    private boolean isEnPassantMove(ChessPiece[][] b, Move last, int fr, int fc, int tr, int tc) {
        ChessPiece pawn = b[fr][fc];
        if (pawn == null || pawn.getType() != ChessPiece.Type.PAWN || b[tr][tc] != null) return false;
        int dir = pawn.getColor() == ChessPiece.Color.WHITE ? -1 : 1;
        if (tr - fr != dir || Math.abs(tc - fc) != 1) return false;
        if (last == null || last.movedPiece == null || last.movedPiece.getType() != ChessPiece.Type.PAWN) return false;
        if (Math.abs(last.toRow - last.fromRow) != 2) return false;
        ChessPiece adjacent = b[fr][tc];
        return adjacent != null && adjacent.getType() == ChessPiece.Type.PAWN && adjacent.getColor() != pawn.getColor()
                && last.toRow == fr && last.toCol == tc;
    }

    private boolean clearPath(ChessPiece[][] b, int fr, int fc, int tr, int tc) {
        int stepR = Integer.compare(tr, fr), stepC = Integer.compare(tc, fc);
        int r = fr + stepR, c = fc + stepC;
        while (r != tr || c != tc) {
            if (b[r][c] != null) return false;
            r += stepR; c += stepC;
        }
        return true;
    }

    public boolean isKingInCheck(ChessPiece.Color color) { return isKingInCheckOnBoard(board, color); }

    private boolean isKingInCheckOnBoard(ChessPiece[][] b, ChessPiece.Color color) {
        int kr = -1, kc = -1;
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) {
            ChessPiece p = b[r][c];
            if (p != null && p.getType() == ChessPiece.Type.KING && p.getColor() == color) { kr = r; kc = c; }
        }
        if (kr == -1) return true;
        return isSquareAttacked(b, kr, kc, opposite(color));
    }

    private boolean isSquareAttacked(ChessPiece[][] b, int row, int col, ChessPiece.Color byColor) {
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) {
            ChessPiece p = b[r][c];
            if (p == null || p.getColor() != byColor) continue;
            if (attacksSquareRaw(b, r, c, row, col)) return true;
        }
        return false;
    }

    private boolean attacksSquareRaw(ChessPiece[][] b, int fr, int fc, int tr, int tc) {
        ChessPiece p = b[fr][fc];
        if (p == null) return false;
        int dr = tr - fr, dc = tc - fc, adr = Math.abs(dr), adc = Math.abs(dc);
        switch (p.getType()) {
            case PAWN:
                int dir = p.getColor() == ChessPiece.Color.WHITE ? -1 : 1;
                return dr == dir && adc == 1;
            case ROOK: return (dr == 0 || dc == 0) && clearPath(b, fr, fc, tr, tc);
            case BISHOP: return adr == adc && clearPath(b, fr, fc, tr, tc);
            case QUEEN: return ((dr == 0 || dc == 0) || adr == adc) && clearPath(b, fr, fc, tr, tc);
            case KNIGHT: return (adr == 2 && adc == 1) || (adr == 1 && adc == 2);
            case KING: return adr <= 1 && adc <= 1;
        }
        return false;
    }

    private void updateGameStatusAfterMove() {
        ArrayList<ComputerPlayer.AIMove> legal = getAllLegalMoves(currentTurn);
        boolean check = isKingInCheck(currentTurn);
        if (legal.isEmpty()) {
            gameOver = true;
            if (check) {
                winner = currentTurn == ChessPiece.Color.WHITE ? blackPlayer : whitePlayer;
                gameStatus = "Checkmate. Winner: " + winner;
            } else {
                winner = "Draw";
                gameStatus = "Stalemate. Game draw.";
            }
        } else if (check) {
            gameStatus = currentTurn + " king is in check";
        } else {
            gameStatus = "Game in progress";
        }
    }

    private Move getLastMove() {
        ArrayList<Move> moves = moveHistory.asMoveList();
        return moves.isEmpty() ? null : moves.get(moves.size() - 1);
    }

    public ArrayList<ComputerPlayer.AIMove> getAllLegalMoves(ChessPiece.Color color) {
        return getAllLegalMovesOnBoard(board, color, getLastMove());
    }

    public ArrayList<ComputerPlayer.AIMove> getAllLegalMovesOnBoard(ChessPiece[][] sourceBoard, ChessPiece.Color color) {
        return getAllLegalMovesOnBoard(sourceBoard, color, null);
    }

    private ArrayList<ComputerPlayer.AIMove> getAllLegalMovesOnBoard(ChessPiece[][] sourceBoard, ChessPiece.Color color, Move lastMove) {
        ArrayList<ComputerPlayer.AIMove> moves = new ArrayList<ComputerPlayer.AIMove>();
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) {
            ChessPiece p = sourceBoard[r][c];
            if (p == null || p.getColor() != color) continue;
            for (int tr = 0; tr < 8; tr++) for (int tc = 0; tc < 8; tc++) {
                if (isLegalMoveOnBoard(sourceBoard, lastMove, r, c, tr, tc)) {
                    ChessPiece target = sourceBoard[tr][tc];
                    boolean enPassantCapture = isEnPassantMove(sourceBoard, lastMove, r, c, tr, tc);
                    int score = target == null ? 0 : ComputerPlayer.pieceValue(target.getType());
                    if (enPassantCapture) score += ComputerPlayer.pieceValue(ChessPiece.Type.PAWN);
                    if (p.getType() == ChessPiece.Type.KING && Math.abs(tc - c) == 2) score += 60;
                    if (p.getType() == ChessPiece.Type.PAWN && (tr == 0 || tr == 7)) score += 850;
                    score += 8 - (Math.abs(3 - tr) + Math.abs(3 - tc));
                    moves.add(new ComputerPlayer.AIMove(r, c, tr, tc, score));
                }
            }
        }
        return moves;
    }

    public String makeComputerMove() {
        if (!isComputerTurn() || gameOver) return "Not computer turn.";
        ComputerPlayer.AIMove aiMove = ComputerPlayer.chooseMove(this, currentTurn, computerDifficulty);
        if (aiMove == null) { updateGameStatusAfterMove(); return gameStatus; }
        return move(aiMove.fromRow, aiMove.fromCol, aiMove.toRow, aiMove.toCol);
    }

    public boolean isKingInCheckOnBoardCopy(ChessPiece[][] sourceBoard, ChessPiece.Color color) { return isKingInCheckOnBoard(sourceBoard, color); }

    public ChessPiece[][] copyBoard() { return copyBoard(board); }
    public ChessPiece[][] copyBoard(ChessPiece[][] source) {
        ChessPiece[][] copy = new ChessPiece[8][8];
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) {
            ChessPiece p = source[r][c];
            if (p != null) { copy[r][c] = new ChessPiece(p.getType(), p.getColor()); copy[r][c].setMoved(p.hasMoved()); }
        }
        return copy;
    }

    public void applyMoveOnBoard(ChessPiece[][] sourceBoard, int fr, int fc, int tr, int tc) {
        applyMoveOnBoard(sourceBoard, null, fr, fc, tr, tc);
    }

    public void applyCurrentMoveOnBoard(ChessPiece[][] sourceBoard, int fr, int fc, int tr, int tc) {
        applyMoveOnBoard(sourceBoard, getLastMove(), fr, fc, tr, tc);
    }

    private void applyMoveOnBoard(ChessPiece[][] sourceBoard, Move last, int fr, int fc, int tr, int tc) {
        ChessPiece piece = sourceBoard[fr][fc];
        if (piece == null) return;
        boolean castling = piece.getType() == ChessPiece.Type.KING && Math.abs(tc - fc) == 2;
        boolean enPassant = isEnPassantMove(sourceBoard, last, fr, fc, tr, tc);
        sourceBoard[tr][tc] = piece;
        sourceBoard[fr][fc] = null;
        piece.setMoved(true);
        if (enPassant) sourceBoard[fr][tc] = null;
        if (castling) {
            int rookFrom = tc > fc ? 7 : 0;
            int rookTo = tc > fc ? 5 : 3;
            ChessPiece rook = sourceBoard[fr][rookFrom];
            sourceBoard[fr][rookTo] = rook;
            sourceBoard[fr][rookFrom] = null;
            if (rook != null) rook.setMoved(true);
        }
        if (piece.getType() == ChessPiece.Type.PAWN && (tr == 0 || tr == 7)) {
            ChessPiece queen = new ChessPiece(ChessPiece.Type.QUEEN, piece.getColor());
            queen.setMoved(true);
            sourceBoard[tr][tc] = queen;
        }
    }

    public boolean isLegalMoveOnBoard(ChessPiece[][] sourceBoard, int fr, int fc, int tr, int tc) {
        return isLegalMoveOnBoard(sourceBoard, null, fr, fc, tr, tc);
    }

    private boolean isLegalMoveOnBoard(ChessPiece[][] sourceBoard, Move lastMove, int fr, int fc, int tr, int tc) {
        if (!isPseudoLegalMove(sourceBoard, lastMove, fr, fc, tr, tc, true)) return false;
        ChessPiece mover = sourceBoard[fr][fc];
        ChessPiece[][] copy = copyBoard(sourceBoard);
        applyMoveOnBoard(copy, lastMove, fr, fc, tr, tc);
        return mover != null && !isKingInCheckOnBoard(copy, mover.getColor());
    }

    public void loadState(String white, String black, ChessPiece.Color turn, ChessPiece[][] loadedBoard) {
        this.whitePlayer = white;
        this.blackPlayer = black;
        this.currentTurn = turn;
        this.board = loadedBoard;
        this.gameOver = false;
        this.winner = "";
        this.gameStatus = "Loaded game";
        this.undoStack.clear();
        this.moveHistory.clear();
        this.capturedWhite.clear();
        this.capturedBlack.clear();
        rebuildTurnQueue();
    }
}
