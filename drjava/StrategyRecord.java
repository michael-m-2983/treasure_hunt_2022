import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.ibm.vie.mazerunner.Location;
import com.ibm.vie.mazerunner.Move;

public class StrategyRecord {
    public ArrayList<Coordinate> path;
    public Deque<Move> moves;

    public StrategyRecord() {
        path = new ArrayList<>();
        moves = new ArrayDeque<>();
    }

    public StrategyRecord(ArrayList<Coordinate> path) {
        this.path = path;
        moves = new ArrayDeque<>();
    }

    // Return the number of moves generated
    public int generateMoves() {
        this.moves.clear();
        if (this.path.size() <= 1) {
            System.err.println("Error, path must have at least two coordinates to generate a movement sequence.");
            return 0;
        }

        Coordinate movingFrom = this.path.get(0);
        for (int i = 1; i < this.path.size(); ++i) {
            Coordinate movingTo = this.path.get(i);

            // Check for illegal moves, duplicates, etc.
            if (movingTo.equals(movingFrom))
                continue;
            if (movingFrom.manhattanDistance(movingTo) > 1) {
                System.err.println("Error, illegal path coordinate (greater than one tile move).");
                break;
            }

            // Calculate which direction to go
            Move m = null;
            if (movingTo.x == movingFrom.x) { // Y axis
                if (movingTo.y > movingFrom.y)
                    m = Move.SOUTH;
                else
                    m = Move.NORTH;
            } else { // X axis
                if (movingTo.x > movingFrom.x)
                    m = Move.EAST;
                else
                    m = Move.WEST;
            }

            // Add the move to the move queue
            moves.add(m);

            movingFrom = movingTo;
        }

        return this.moves.size();
    }

    public Move getNextMove() {
        return this.moves.removeFirst();
    }

    public boolean doneMoving() {
        return this.moves.size() == 0;
    }
}
