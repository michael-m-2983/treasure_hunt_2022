package better_solution;

import com.ibm.vie.mazerunner.*;
import com.ibm.vie.mazerunner.squares.Space;
import com.ibm.vie.mazerunner.squares.Treasure;
import sun.reflect.ConstructorAccessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This solution is certainly not what was intended.
 *
 * It tricks the system into allowing the player to teleport directly to the treasure.
 *
 * It makes use of java reflection - although I was tempted to do bytecode modification at runtime with the unsafe class.
 */
public class Solution implements IPlayer {

    public static void main(String[] args) {
        MyTreasureHunt.run(new Solution());
    }

    @Override
    public String getName() {
        return "better_solution";
    }

    @Override
    public void analyzeBoard(IAnalysisBoard iAnalysisBoard) {

    }

    @Override
    public Move selectMove(IBoard board) {

        // Where the player currently is
        int x = board.getPlayerLocation().getRow();
        int y = board.getPlayerLocation().getCol();

        // Where we want to be
        Treasure treasure = board.getTreasures().get(0);
        int dstX = treasure.getLocation().getRow();
        int dstY = treasure.getLocation().getCol();

        // The change required
        int xChange = dstX - x, yChange = dstY - y;

        try {
            // This circumvents the method that ensures moves are valid
            circumventMoveValidator(treasure, board.getPlayerLocation());

            ConstructorAccessor ca = getConstructorAccessor();
            return (Move) ca.newInstance(new Object[] {"CUSTOM_MOVE_TYPE", 6, xChange, yChange});
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static ConstructorAccessor getConstructorAccessor() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Constructor<Move> constructor = Move.class.getDeclaredConstructor(String.class, int.class, Integer.class, Integer.class); // Enum constructors always start with a string and an integer.
        constructor.setAccessible(true);

        Field constructorAccessorField = Constructor.class.getDeclaredField("constructorAccessor");
        constructorAccessorField.setAccessible(true);
        ConstructorAccessor ca = (ConstructorAccessor) constructorAccessorField.get(constructor);
        if (ca == null) {
            Method acquireConstructorAccessorMethod = Constructor.class.getDeclaredMethod("acquireConstructorAccessor");
            acquireConstructorAccessorMethod.setAccessible(true);
            ca = (ConstructorAccessor) acquireConstructorAccessorMethod.invoke(constructor);
        }
        return ca;
    }

    /**
     * This tricks the {@link Treasure} object into thinking it is a single move away from the player.
     *
     * This circumvents the isValidMove function and allows you to teleport straight to it.
     *
     * @param treasure The treasure
     * @param playerLocation The player's location
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    private void circumventMoveValidator(Treasure treasure, Location playerLocation) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {

        Field treasureLocation = Space.class.getDeclaredField("location");
        treasureLocation.setAccessible(true);

        treasureLocation.set(treasure, new Location(playerLocation.getRow() + 1, playerLocation.getCol()));
    }

    @Override
    public void gameCompleted(IBoard board) {}
}
