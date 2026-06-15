public class TurnQueue {
    private static class Node {
        ChessPiece.Color color;
        Node next;
        Node(ChessPiece.Color color) { this.color = color; }
    }

    private Node front, rear;

    public void enqueue(ChessPiece.Color color) {
        Node n = new Node(color);
        if (rear == null) front = rear = n;
        else { rear.next = n; rear = n; }
    }

    public ChessPiece.Color dequeue() {
        if (front == null) return null;
        ChessPiece.Color c = front.color;
        front = front.next;
        if (front == null) rear = null;
        return c;
    }

    public ChessPiece.Color peek() { return front == null ? null : front.color; }
    public void clear() { front = rear = null; }
}
