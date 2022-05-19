import java.util.*;

import com.ibm.vie.mazerunner.IAnalysisBoard;
import com.ibm.vie.mazerunner.squares.Treasure;

public class Pathfinding {
    // Structure for storing a coordinate along with its priority for Dijkstra
    // Also includes a comparator so the PriorityQueue uses its "natural ordering"
    // without the need for a comparator.
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

    // Valid walking directions
    public static final Coordinate[] offsets = {
            new Coordinate(-1, 0),
            new Coordinate(1, 0),
            new Coordinate(0, -1),
            new Coordinate(0, 1),
    };

    // Implementation of Dijkstra's algorithm for weighted shortest-path
    public static void dijkstra(IAnalysisBoard board, Coordinate start,
            HashMap<Coordinate, Integer> distance, HashMap<Coordinate, Coordinate> prev, boolean shortCircuit,
            Coordinate[] targets) {
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

        ArrayList<Coordinate> tgts = new ArrayList<>();
        for (Coordinate t : targets)
            tgts.add(t);

        while (Q.size() > 0) {
            Coordinate c = Q.remove().c;
            if (shortCircuit && tgts.size() == 0)
                break;

            tgts.remove(c);

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
    }

    // Heuristic distance function for A*;
    private static int heuristic(Coordinate a, Coordinate b) {
        return a.manhattanDistance(b);
    }

    // Improved version of Dijkstra's algorithm using a heuristic distance function
    // to sort candidates.
    // The only issue is that due to sorting, it only works efficiently for one
    // target
    public static void Astar(IAnalysisBoard board, Coordinate start,
            HashMap<Coordinate, Integer> distance, HashMap<Coordinate, Coordinate> prev, Coordinate target) {

        PriorityQueue<CoordinateInfo> Q = new PriorityQueue<>();
        HashSet<Coordinate> visited = new HashSet<>();

        int width = board.getWidth(), height = board.getHeight();

        for (int x = 0; x < width; ++x)
            for (int y = 0; y < height; ++y) {
                Coordinate c = new Coordinate(x, y);
                distance.put(c, Integer.MAX_VALUE);
                prev.put(c, null);
            }

        // Make sure the starting square is evaulated first.
        Q.add(new CoordinateInfo(start, 0));
        distance.put(start, 0);

        while (Q.size() > 0) {
            Coordinate c = Q.remove().c;
            if (c.equals(target))
                break;

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
                        // Prioritize the "candidate" neighbors based on distance heuristic to the
                        // target.
                        Q.add(new CoordinateInfo(nbr, newdist + heuristic(nbr, target)));
                    }
                }
            }
        }
    }

    // Given the table linking tiles to the tile walked before it, and a target
    // coordinate, this function returns the already-generated shortest path between
    // them by walking backwards.
    public static ArrayList<Coordinate> pathFromPrev(Coordinate dest, HashMap<Coordinate, Coordinate> prev) {
        ArrayList<Coordinate> spath = new ArrayList<>();
        spath.add(dest);

        Coordinate i = prev.get(dest);
        while (i != null) {
            spath.add(i);
            i = prev.get(i);
        }

        Collections.reverse(spath);
        return spath;
    }

    // Wrapper function for simple point-to-point pathfinding.
    public static ArrayList<Coordinate> shortestPath(IAnalysisBoard board, Coordinate start, Coordinate destination) {
        HashMap<Coordinate, Coordinate> prev = new HashMap<>();
        HashMap<Coordinate, Integer> distance = new HashMap<>();
        Astar(board, start, distance, prev, destination);
        return pathFromPrev(destination, prev);
    }

    // Base class for "treasure distance comparators" which basically sort treasures
    // taking into account the current position of the agent.
    public static abstract class TreasureDistanceComparator implements Comparator<Treasure> {
        // The results from `compare` should depend on the Treasures' relation to this
        // "origin" point (the agent, usually)
        public Coordinate sort_origin;

        public TreasureDistanceComparator() {
        }

        public TreasureDistanceComparator(Coordinate c) {
            this.sort_origin = c;
        }

        // Used to do calculations ahead of time, if needed
        public void init(IAnalysisBoard board) {
        }

        // Make sure to make CLOSER treasures HIGHER than FARTHER ones because we want
        // the closest to be at the END of the list.
        // (for dynamic array removal efficiency's sake, removing from the front usually
        // causes the whole array to be shifted which is O(n))
        @Override
        public abstract int compare(Treasure o1, Treasure o2);
    }

    // Comparator for a faster heuristic selection
    public static class ManhattanHeuristicSelector extends TreasureDistanceComparator {
        @Override
        public int compare(Treasure o1, Treasure o2) {
            // The comparison is a crude manhattan distance, can end up being very
            // inefficient when say, a treasure is on the other side of a wall but closer
            // than one on the same side.
            return -(new Coordinate(o1.getLocation()).manhattanDistance(sort_origin) -
                    new Coordinate(o2.getLocation()).manhattanDistance(sort_origin));
        }
    }

    // More accurate selector using actual distance, with the added advantage of
    // being able to cache paths for later use.
    public static class ActualCostSelector extends TreasureDistanceComparator {
        // Table of "actual" (including pathfinding-avoided obstacles) distances between
        // treasure coordinates. Used for caching path segments that can be used when
        // generating the full path
        public HashMap<List<Coordinate>, Integer> edgeDistanceMesh;
        // Path caching
        public HashMap<List<Coordinate>, ArrayList<Coordinate>> actualEdges;

        @Override
        public void init(IAnalysisBoard board) {
            this.edgeDistanceMesh = new HashMap<>();
            this.actualEdges = new HashMap<>();

            // Which coordinates are we measuring to/from
            ArrayList<Coordinate> measurements = new ArrayList<>();
            for (Treasure t : board.getTreasures())
                measurements.add(new Coordinate(t.getLocation()));
            measurements.add(new Coordinate(board.getStartingLocation()));

            // Coordinates that haven't been mapped to every other coordinate yet
            ArrayList<Coordinate> targetsRemaining = new ArrayList<>();
            for (Coordinate c : measurements)
                targetsRemaining.add(c);

            for (Coordinate c : measurements) {
                HashMap<Coordinate, Coordinate> prev = new HashMap<>();
                HashMap<Coordinate, Integer> distance = new HashMap<>();
                dijkstra(board, c, distance, prev, true, targetsRemaining.toArray(new Coordinate[0]));
                for (Coordinate tgt : targetsRemaining) {
                    edgeDistanceMesh.put(Arrays.asList(c, tgt), distance.get(tgt));
                    actualEdges.put(Arrays.asList(c, tgt), pathFromPrev(tgt, prev));
                }

                targetsRemaining.remove(c);
            }
        }

        public int getDistance(Coordinate a, Coordinate b) {
            return edgeDistanceMesh.containsKey(Arrays.asList(b, a)) ? edgeDistanceMesh.get(Arrays.asList(b, a))
                    : (edgeDistanceMesh.containsKey(Arrays.asList(a, b)) ? edgeDistanceMesh.get(Arrays.asList(a, b))
                            : -1);
        }

        public ArrayList<Coordinate> getEdgePath(Coordinate src, Coordinate dest) {
            ArrayList<Coordinate> ep = actualEdges.containsKey(Arrays.asList(dest, src))
                    ? actualEdges.get(Arrays.asList(dest, src))
                    : (actualEdges.containsKey(Arrays.asList(src, dest)) ? actualEdges.get(
                            Arrays.asList(src, dest)) : null);

            // The path needs to be reversed if it was entered into the cache backwards
            // (which is done to remove "duplicates")
            if (actualEdges.containsKey(Arrays.asList(dest, src)))
                Collections.reverse(ep);
            return ep;
        }

        // Negated because we want the closest to be at the END of the list
        // (for removal efficiency's sake)
        @Override
        public int compare(Treasure o1, Treasure o2) {
            return -(getDistance(new Coordinate(o1.getLocation()), sort_origin) -
                    getDistance(new Coordinate(o2.getLocation()), sort_origin));
        }
    }

    public HashMap<Treasure, Treasure> getAdjacencyList(IAnalysisBoard board) {
        HashMap<Treasure, Treasure> edges = new HashMap<>();

        List<Treasure> treasures = board.getTreasures();
        for (Treasure a : treasures)
            for (Treasure b : treasures)
                if (a != b)
                    edges.put(a, b);

        return edges;
    }

    public ArrayList<Treasure> getSortedTreasures(IAnalysisBoard board) {
        ArrayList<Treasure> treasures = new ArrayList<>();

        // Reuse ActualCostSelector to keep track of distances between the nodes
        ActualCostSelector distances = new ActualCostSelector();
        distances.init(board);

        return treasures;
    }

    // The simplest and stupidest strategy to the traveling-salesman problem that
    // is the treasure hunt. Simply pathfinds to the closest target and repeats
    // doing so until there are no treasures left.
    public static StrategyRecord greedyPathfindingNetwork(IAnalysisBoard board, TreasureDistanceComparator cmp) {
        StrategyRecord strat = new StrategyRecord();
        // Where the agent "currently" is
        Coordinate position = new Coordinate(board.getStartingLocation());

        ArrayList<Treasure> treasures = new ArrayList<>();
        treasures.addAll(board.getTreasures());

        // Let the comparator see the board
        cmp.init(board);

        // Go through all of the treasures
        while (treasures.size() > 0) {
            // Sort the remaining treasures with the comparator
            cmp.sort_origin = position;
            Collections.sort(treasures, cmp);

            // Find the best candidate and pop it.
            Treasure target = treasures.remove(treasures.size() - 1);

            ArrayList<Coordinate> travel;
            // If cmp is an ActualCostSelector, it already has the path cached, so we use
            // it!
            if (cmp instanceof ActualCostSelector)
                travel = ((ActualCostSelector) cmp)
                        .getEdgePath(position, new Coordinate(target.getLocation()));
            else // Otherwise we just calculate it on-the-fly
                travel = shortestPath(board, position, new Coordinate(target.getLocation()));

            strat.path.addAll(travel);

            // Set the agent's current position to where it just went
            position = new Coordinate(target.getLocation());
        }

        System.out.printf("Planning to complete board \"%s\" in %d moves.\n", board.getName(), strat.generateMoves());
        return strat;
    }
}