public class MoveStack {
    private static class Node {
        Move move;
        Node next;
        Node(Move move) { this.move = move; }
    }

    private Node top;
    private int size;

    public void push(Move move) {
        Node n = new Node(move);
        n.next = top;
        top = n;
        size++;
    }

    public Move pop() {
        if (top == null) return null;
        Move m = top.move;
        top = top.next;
        size--;
        return m;
    }

    public boolean isEmpty() { return top == null; }
    public int size() { return size; }
    public void clear() { top = null; size = 0; }
}
