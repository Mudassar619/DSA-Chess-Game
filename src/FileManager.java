import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileManager {
    private static final File savedGame = new File("saved_game.txt");
    private static final File matchHistory = new File("match_history.txt");

    public static void saveGame(ChessGame game) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(savedGame))) {
            pw.println(game.getWhitePlayer());
            pw.println(game.getBlackPlayer());
            pw.println(game.getCurrentTurn().name());
            for (int r = 0; r < 8; r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < 8; c++) {
                    ChessPiece p = game.getPiece(r, c);
                    sb.append(p == null ? "--" : p.shortCode());
                    if (c < 7) sb.append(" ");
                }
                pw.println(sb);
            }
        }
    }

    public static boolean loadGame(ChessGame game) throws IOException {
        if (!savedGame.exists()) return false;
        try (BufferedReader br = new BufferedReader(new FileReader(savedGame))) {
            String white = br.readLine();
            String black = br.readLine();
            ChessPiece.Color turn = ChessPiece.Color.valueOf(br.readLine());
            ChessPiece[][] board = new ChessPiece[8][8];
            for (int r = 0; r < 8; r++) {
                String[] cells = br.readLine().split(" ");
                for (int c = 0; c < 8; c++) board[r][c] = ChessPiece.fromCode(cells[c]);
            }
            game.loadState(white, black, turn, board);
            return true;
        }
    }

    public static void appendMatch(String white, String black, String winner, int totalMoves) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
        try (PrintWriter pw = new PrintWriter(new FileWriter(matchHistory, true))) {
            pw.println(white + "," + black + "," + winner + "," + date + "," + totalMoves + " moves");
        } catch (IOException e) { System.out.println("Could not save match history: " + e.getMessage()); }
    }

    public static java.util.List<String[]> readMatchHistory() {
        java.util.List<String[]> rows = new ArrayList<>();
        if (!matchHistory.exists()) return rows;
        try (BufferedReader br = new BufferedReader(new FileReader(matchHistory))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 5) rows.add(p);
            }
        } catch (IOException e) { System.out.println("Could not read match history: " + e.getMessage()); }
        return rows;
    }

    public static File exportMatchReport(ChessGame game) throws IOException {
        File report = new File("match_report.txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(report))) {
            pw.println("Chess Match Report");
            pw.println("White: " + game.getWhitePlayer());
            pw.println("Black: " + game.getBlackPlayer());
            pw.println("Current Turn: " + game.getCurrentTurn());
            pw.println("Total Moves: " + game.getMoveHistory().size());
            pw.println();
            for (String m : game.getMoveHistory().asTextList()) pw.println(m);
        }
        return report;
    }
}
