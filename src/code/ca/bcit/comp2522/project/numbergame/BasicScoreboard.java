package ca.bcit.comp2522.project.numbergame;

/**
 * A Class that provides a concrete instance of basic scoreboard implementation.
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class BasicScoreboard extends AbstractNumberGame
{
    /**
     * Resets the game to its initial state.
     * <p>
     * In this basic scoreboard implementation, this method is intentionally left blank.
     * Subclasses may override this method to perform game-specific reset operations.
     * </p>
     */
    @Override
    public void resetGame() {}

    /**
     * Checks if the game is lost.
     * <p>
     * In this basic scoreboard implementation, this method is intentionally left blank.
     * Subclasses may override this method to implement game-specific loss conditions.
     * </p>
     */
    @Override
    public void checkIfLost() {}
}