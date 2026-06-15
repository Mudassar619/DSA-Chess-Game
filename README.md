# DSA-Chess-Game
How to run:
1. Open this folder.
2. Double-click run_game.bat on Windows, or run:
   java -jar DSAChessGame.jar

Main features:
- Full chess board with legal move validation.
- Check, checkmate, stalemate, castling, en passant, pawn promotion, undo, save/load, move history, captured pieces, and AI modes.
- AI uses priority queue ordering, minimax, alpha-beta pruning, and HashMap board-state caching.
- Scoreboard and match history saving.
- Export match report.
- Minimax Game Tree panel in the main menu.
  It builds a limited minimax search tree from the current board position.
  Each node shows a possible move, tactical score, and minimax value.
- Match History Network panel in the main menu.
  It displays players as graph vertices and completed matches as graph edges.

DSA Principals used in this project
- Stack: Used for undo move history.
- Linked List: Used for move history tracking.
- Queue: Used for turn management.
- Priority Queue: Used for computer move ordering.
- HashMap: Used for AI board-state caching.
- Minimax + Alpha-Beta: Used for Hard computer mode.
- Tree: Used for Visualizing AI Search Process.
- Graph: Used for Match History Network Visualization.

Files:
- src/: Java source files
- DSAChessGame.jar: runnable game jar
- run_game.bat: Windows launcher
