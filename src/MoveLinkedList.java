import java.util.ArrayList;
import java.util.List;

public class MoveLinkedList {
    private static class Node {
        Move move;
        Node next;
        Node(Move move) { this.move = move; }
    }

    private Node head;
    private Node tail;
    private int size;

    public void add(Move move) {
        Node n = new Node(move);
        if (head == null) head = tail = n;
        else { tail.next = n; tail = n; }
        size++;
    }

    public Move removeLast() {
        if (head == null) return null;
        if (head == tail) {
            Move m = head.move;
            head = tail = null;
            size = 0;
            return m;
        }
        Node current = head;
        while (current.next != tail) current = current.next;
        Move m = tail.move;
        tail = current;
        tail.next = null;
        size--;
        return m;
    }

    public List<String> asTextList() {
        List<String> result = new ArrayList<>();
        Node cur = head;
        int i = 1;
        while (cur != null) {
            result.add(i + ". " + cur.move.simpleText());
            cur = cur.next;
            i++;
        }
        return result;
    }

    public ArrayList<Move> asMoveList() {
        ArrayList<Move> result = new ArrayList<>();
        Node cur = head;
        while (cur != null) {
            result.add(cur.move);
            cur = cur.next;
        }
        return result;
    }

    public int size() { return size; }
    public void clear() { head = tail = null; size = 0; }
}
