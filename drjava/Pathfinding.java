import java.util.*;

import com.ibm.vie.mazerunner.IAnalysisBoard;
import com.ibm.vie.mazerunner.squares.Treasure;

public class Pathfinding {
    public static class CoordinateInfo implements Comparable<CoordinateInfo> {
        public Coordinate c;
        public int priority;

        public CoordinateInfo(Coordinate c) {
            this.c = c;
        }

        public CoordinateInfo(Coordinate c, int p) {
            this.c = c;
            this.priority = p;
        }

        @Override
        public int compareTo(CoordinateInfo o) {
            return this.priority - o.priority;
        }
    }

    public static final Coordinate[] offsets = {
            new Coordinate(-1, 0),
            new Coordinate(1, 0),
            new Coordinate(0, -1),
            new Coordinate(0, 1),
    };

    // Implementation of Dijkstra's algorithm for weighted shortest-path
    public static ArrayList<Coordinate> shortestPath(IAnalysisBoard board, Coordinate start, Coordinate destination) {
        ArrayList<Coordinate> spath = new ArrayList<>();
        HashMap<Coordinate, Coordinate> prev = new HashMap<>();
        HashMap<Coordinate, Integer> distance = new HashMap<>();
        PriorityQueue<CoordinateInfo> Q = new PriorityQueue<>();
        HashSet<Coordinate> visited = new HashSet<>();

        int width = board.getWidth(), height = board.getHeight();

        for (int x = 0; x < width; ++x)
            for (int y = 0; y < height; ++y) {
                Coordinate c = new Coordinate(x, y);
                distance.put(c, Integer.MAX_VALUE);
                prev.put(c, null);
                Q.add(new CoordinateInfo(c, c.equals(start) ? 0 : Integer.MAX_VALUE));
            }

        distance.put(start, 0);

        // System.out.println("Beginning traversal.");

        while (Q.size() > 0) {
            Coordinate c = Q.remove().c;
            if (c.equals(destination)) {
                break;
            }

            if (visited.contains(c))
                continue;
            visited.add(c);

            for (Coordinate o : offsets) {
                // Tile in bounds
                Coordinate nbr = c.add(o);
                // System.out.println(nbr);
                if (nbr.x >= 0 && nbr.y >= 0 && nbr.x < width && nbr.y < height) {
                    int cost = board.getSquareAt(nbr.location()).getStepCost();
                    if (cost == Integer.MAX_VALUE)
                        cost = 1024; // Prevent overflows

                    int newdist = distance.get(c) + cost;
                    if (newdist < distance.get(nbr)) {
                        distance.put(nbr, newdist);
                        prev.put(nbr, c);
                        Q.add(new CoordinateInfo(nbr, newdist));
                    }
                }
            }
        }

        // System.out.println("Done!");

        spath.add(destination);

        Coordinate i = prev.get(destination);
        while (i != null) {
            spath.add(i);
            i = prev.get(i);
        }

        // Reverse the path
        for (int p = 0; p < spath.size() / 2; ++p) {
            i = spath.get(spath.size() - 1 - p);
            spath.set(spath.size() - 1 - p, spath.get(p));
            spath.set(p, i);
        }

        // for (Coordinate c : spath)
        // System.out.println(c.toString());

        return spath;
    }

    public static Treasure[] getSortedTreasures(IAnalysisBoard board, Coordinate origin) {
        Treasure treasures[] = (Treasure[]) board.getTreasures().toArray();
        Arrays.sort(treasures, Comparator.comparing(t -> t.getLocation().distance(origin.location())));
        return treasures;
    }

    // public static class TreasureDistanceComparator implements
    // Comparator<Treasure> {
    // public Coordinate sort_origin;

    // public TreasureDistanceComparator() {
    // }

    // public TreasureDistanceComparator(Coordinate c) {
    // this.sort_origin = c;
    // }

    // // Negated because we want the closest to be at the END of the list
    // // (for removal efficiency's sake)
    // @Override
    // public int compare(Treasure o1, Treasure o2) {
    // // TODO Auto-generated method stub
    // return -(new Coordinate(o1.getLocation()).manhattanDistance(sort_origin)
    // - new Coordinate(o2.getLocation()).manhattanDistance(sort_origin));
    // }

    // }

    public static abstract class TreasureDistanceComparator implements Comparator<Treasure> {
        public Coordinate sort_origin;

        public TreasureDistanceComparator() {

        }

        public TreasureDistanceComparator(Coordinate c) {
            this.sort_origin = c;
        }

        public void init(IAnalysisBoard board) {

        }

        // Make sure to make CLOSER treasures HIGHER than FARTHER ones.
        @Override
        public abstract int compare(Treasure o1, Treasure o2);
    }

    public static class ManhattanHeuristicSelector extends TreasureDistanceComparator {
        // Negated because we want the closest to be at the END of the list
        // (for removal efficiency's sake)
        @Override
        public int compare(Treasure o1, Treasure o2) {
            return -(new Coordinate(o1.getLocation()).manhattanDistance(sort_origin) -
                    new Coordinate(o2.getLocation()).manhattanDistance(sort_origin));
        }
    }

    public static class ActualCostSelector extends TreasureDistanceComparator {
        HashMap<List<Coordinate>, Integer> edgeDistanceMesh;

        @Override
        public void init(IAnalysisBoard board) {
            this.edgeDistanceMesh = new HashMap<>();
            ArrayList<Coordinate> treasures = new ArrayList<>();
            for (Treasure t : board.getTreasures())
                treasures.add(new Coordinate(t.getLocation()));
            treasures.add(new Coordinate(board.getStartingLocation()));

            for (Coordinate a : treasures)
                for (Coordinate b : treasures)
                    if (!a.equals(b) && !edgeDistanceMesh.containsKey(Arrays.asList(a, b)) && !edgeDistanceMesh
                            .containsKey(Arrays.asList(b, a)))
                        edgeDistanceMesh.put(Arrays.asList(a, b), shortestPath(board, a, b).size() - 1);
        }

        public int getDistance(Coordinate a, Coordinate b) {
            return edgeDistanceMesh.containsKey(Arrays.asList(b, a)) ? edgeDistanceMesh.get(Arrays.asList(b, a))
                    : edgeDistanceMesh.get(Arrays.asList(a, b));
        }

        // Negated because we want the closest to be at the END of the list
        // (for removal efficiency's sake)
        @Override
        public int compare(Treasure o1, Treasure o2) {
            return -(getDistance(new Coordinate(o1.getLocation()), sort_origin) -
                    getDistance(new Coordinate(o2.getLocation()), sort_origin));
        }
    }

    public static StrategyRecord pathfindingNetwork(IAnalysisBoard board, TreasureDistanceComparator cmp) {
        StrategyRecord strat = new StrategyRecord();
        Coordinate position = new Coordinate(board.getStartingLocation());

        ArrayList<Treasure> treasures = new ArrayList<>();
        treasures.addAll(board.getTreasures());

        // Let the comparator see the board
        cmp.init(board);

        while (treasures.size() > 0) {
            // Sort the remaining treasures
            cmp.sort_origin = position;
            Collections.sort(treasures, cmp);

            Treasure target = treasures.remove(treasures.size() - 1);

            ArrayList<Coordinate> travel = shortestPath(
                    board,
                    position,
                    new Coordinate(target.getLocation()));

            strat.path.addAll(travel);

            position = new Coordinate(target.getLocation());
        }

        System.out.printf("Planning to complete the board in %d moves.\n", strat.generateMoves());
        return strat;
    }

    public static void main(String[] args) {
        PriorityQueue<CoordinateInfo> Q = new PriorityQueue();
        Q.add(new CoordinateInfo(new Coordinate(0, 1), 1));
        Q.add(new CoordinateInfo(new Coordinate(0, 3), 3));
        Q.add(new CoordinateInfo(new Coordinate(0, 2), 2));

        CoordinateInfo i;
        while (!Q.isEmpty() && (i = Q.remove()) != null) {
            System.out.println(i.c);
        }

        System.out.println(new Coordinate(0, 2).hashCode() == new Coordinate(0, 2).hashCode());
    }
}