import com.ibm.vie.mazerunner.Location;

public class Coordinate {
    public int x, y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate() {
        this.x = this.y = 0;
    }

    public Coordinate(Location l) {
        this.x = l.getCol();
        this.y = l.getRow();
    }

    public Location location() {
        return new Location(y, x);
    }

    public Coordinate add(Coordinate o) {
        return new Coordinate(x + o.x, y + o.y);
    }

    public Coordinate sub(Coordinate o) {
        return new Coordinate(x - o.x, y - o.y);
    }

    public Coordinate negative() {
        return new Coordinate(-x, -y);
    }

    public double euclideanDistance(Coordinate o) {
        return Math.hypot(this.x - o.x, this.y - o.y);
    }

    public int manhattanDistance(Coordinate o) {
        return Math.abs(x - o.x) + Math.abs(y - o.y);
    }

    // Swaps X and Y axes
    public void transpose() {
        int t = this.x;
        this.x = this.y;
        this.y = t;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    @Override
    public int hashCode() {
        return (x << 8) | y;
    }

    @Override
    public boolean equals(Object O) {
        Coordinate o = (Coordinate) O;
        return this.x == o.x && this.y == o.y;
    }
}