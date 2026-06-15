import java.io.*;
import java.util.*;

public class ScoreManager {
    public static class PlayerRecord {
        String name;
        int wins, losses, draws, matches;
        PlayerRecord(String name) { this.name = name; }
    }

    private final HashMap<String, PlayerRecord> records = new HashMap<>();
    private final File file = new File("players.txt");

    public ScoreManager() { load(); }

    public void ensurePlayer(String name) {
        records.putIfAbsent(name.toLowerCase(), new PlayerRecord(name));
    }

    public void recordMatch(String white, String black, String winner) {
        ensurePlayer(white); ensurePlayer(black);
        PlayerRecord w = records.get(white.toLowerCase());
        PlayerRecord b = records.get(black.toLowerCase());
        w.matches++; b.matches++;
        if (winner == null || winner.equalsIgnoreCase("Draw")) {
            w.draws++; b.draws++;
        } else if (winner.equals(white)) {
            w.wins++; b.losses++;
        } else if (winner.equals(black)) {
            b.wins++; w.losses++;
        }
        save();
    }

    public List<PlayerRecord> sortedRecords() {
        List<PlayerRecord> list = new ArrayList<>(records.values());
        list.sort((a, b) -> b.wins != a.wins ? b.wins - a.wins : b.matches - a.matches);
        return list;
    }

    public PlayerRecord search(String name) { return records.get(name.toLowerCase()); }

    public void load() {
        records.clear();
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 5) {
                    PlayerRecord r = new PlayerRecord(p[0]);
                    r.wins = Integer.parseInt(p[1]);
                    r.losses = Integer.parseInt(p[2]);
                    r.draws = Integer.parseInt(p[3]);
                    r.matches = Integer.parseInt(p[4]);
                    records.put(r.name.toLowerCase(), r);
                }
            }
        } catch (Exception e) { System.out.println("Could not load players: " + e.getMessage()); }
    }

    public void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (PlayerRecord r : records.values()) {
                pw.println(r.name + "," + r.wins + "," + r.losses + "," + r.draws + "," + r.matches);
            }
        } catch (IOException e) { System.out.println("Could not save players: " + e.getMessage()); }
    }
}
