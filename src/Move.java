public class Move {
    public final ChessPiece movedPiece;
    public final ChessPiece capturedPiece;
    public final int fromRow, fromCol, toRow, toCol;
    public final boolean movedBefore;
    public final boolean promotion;
    public final ChessPiece originalPawn;

    public final boolean castling;
    public final int rookFromRow, rookFromCol, rookToRow, rookToCol;
    public final boolean rookMovedBefore;

    public final boolean enPassant;
    public final int capturedRow, capturedCol;

    public Move(ChessPiece movedPiece, ChessPiece capturedPiece, int fromRow, int fromCol, int toRow, int toCol,
                boolean movedBefore, boolean promotion, ChessPiece originalPawn) {
        this(movedPiece, capturedPiece, fromRow, fromCol, toRow, toCol, movedBefore, promotion, originalPawn,
                false, -1, -1, -1, -1, false, false, -1, -1);
    }

    public Move(ChessPiece movedPiece, ChessPiece capturedPiece, int fromRow, int fromCol, int toRow, int toCol,
                boolean movedBefore, boolean promotion, ChessPiece originalPawn,
                boolean castling, int rookFromRow, int rookFromCol, int rookToRow, int rookToCol, boolean rookMovedBefore,
                boolean enPassant, int capturedRow, int capturedCol) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.movedBefore = movedBefore;
        this.promotion = promotion;
        this.originalPawn = originalPawn;
        this.castling = castling;
        this.rookFromRow = rookFromRow;
        this.rookFromCol = rookFromCol;
        this.rookToRow = rookToRow;
        this.rookToCol = rookToCol;
        this.rookMovedBefore = rookMovedBefore;
        this.enPassant = enPassant;
        this.capturedRow = capturedRow;
        this.capturedCol = capturedCol;
    }

    public String simpleText() {
        char fromFile = (char) ('A' + fromCol);
        char toFile = (char) ('A' + toCol);
        return movedPiece + " " + fromFile + (8 - fromRow) + " → " + toFile + (8 - toRow)
                + (capturedPiece != null ? " captured " + capturedPiece : "")
                + (castling ? " castling" : "")
                + (enPassant ? " en passant" : "")
                + (promotion ? " promoted" : "");
    }

    public String toCSV() {
        return movedPiece + "," + fromRow + "," + fromCol + "," + toRow + "," + toCol + "," +
                (capturedPiece == null ? "None" : capturedPiece.toString()) + "," + promotion + "," + castling + "," + enPassant;
    }
}
