public class ChessPiece {
    public enum Color { WHITE, BLACK }
    public enum Type { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

    private final Type type;
    private final Color color;
    private boolean moved;

    public ChessPiece(Type type, Color color) {
        this.type = type;
        this.color = color;
        this.moved = false;
    }

    public Type getType() { return type; }
    public Color getColor() { return color; }
    public boolean hasMoved() { return moved; }
    public void setMoved(boolean moved) { this.moved = moved; }

    public String symbol() {
        if (color == Color.WHITE) {
            switch (type) {
                case KING: return "♔";
                case QUEEN: return "♕";
                case ROOK: return "♖";
                case BISHOP: return "♗";
                case KNIGHT: return "♘";
                case PAWN: return "♙";
            }
        } else {
            switch (type) {
                case KING: return "♚";
                case QUEEN: return "♛";
                case ROOK: return "♜";
                case BISHOP: return "♝";
                case KNIGHT: return "♞";
                case PAWN: return "♟";
            }
        }
        return "";
    }

    public String shortCode() {
        String c = color == Color.WHITE ? "W" : "B";
        return c + type.name().charAt(0) + (moved ? "1" : "0");
    }

    public static ChessPiece fromCode(String code) {
        if (code == null || code.equals("--") || code.length() < 3) return null;
        Color color = code.charAt(0) == 'W' ? Color.WHITE : Color.BLACK;
        char t = code.charAt(1);
        Type type;
        switch (t) {
            case 'K': type = Type.KING; break;
            case 'Q': type = Type.QUEEN; break;
            case 'R': type = Type.ROOK; break;
            case 'B': type = Type.BISHOP; break;
            case 'N': type = Type.KNIGHT; break;
            case 'P': type = Type.PAWN; break;
            default: return null;
        }
        ChessPiece p = new ChessPiece(type, color);
        p.setMoved(code.charAt(2) == '1');
        return p;
    }

    @Override
    public String toString() {
        return (color == Color.WHITE ? "White" : "Black") + " " + type.name();
    }
}
