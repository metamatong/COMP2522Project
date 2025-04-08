package ca.bcit.comp2522.project.numbergame;

/**
 * The {@code NumberGameStats} interface defines a contract for tracking and reporting
 * statistical data related to a number-based game. Implementations of this interface are
 * responsible for maintaining counts of games played, wins, losses, and placements, as well
 * as providing calculations (such as the average placements per game) and generating a formatted
 * summary of the overall game performance.
 * <p>
 * Typical implementations might be used to provide a scoreboard that is updated after each game,
 * allowing for historical performance tracking over multiple sessions.
 * </p>
 *
 * @author Kyle Cheon
 * @version 1.0
 */
interface NumberGameStats
{
    /**
     * Retrieves the total number of games played.
     *
     * @return the total number of games played
     */
    int getTotalGamesPlayed();

    /**
     * Retrieves the total number of wins.
     *
     * @return the total number of wins
     */
    int getTotalWins();

    /**
     * Retrieves the total number of losses.
     *
     * @return the total number of losses
     */
    int getTotalLosses();

    /**
     * Retrieves the total number of placements.
     *
     * @return the total number of placements
     */
    int getTotalPlacements();

    /**
     * Calculates and returns the average number of placements per game.
     *
     * @return the average placements per game
     */
    double getAveragePlacementsPerGame();

    /**
     * Increments the total count of games played by one.
     */
    void incrementGamesPlayed();

    /**
     * Increments the total count of wins by one.
     */
    void incrementWins();

    /**
     * Increments the total count of losses by one.
     */
    void incrementLosses();

    /**
     * Adds the specified number of placements from the current game to the total placements.
     *
     * @param placementsThisGame the number of placements to add
     */
    void addToTotalPlacements(int placementsThisGame);

    /**
     * Returns a summary of the current scoreboard statistics.
     * <p>
     * The summary typically includes the total games played, wins, losses, total placements,
     * and the average placements per game.
     * </p>
     *
     * @return a formatted string summarizing the scoreboard
     */
    String getScoreboardSummary();

    /**
     * Resets the game to its initial state, including resetting all tracked statistics.
     */
    void resetGame();

    /**
     * Checks if the current game state qualifies as a loss.
     * <p>
     * Implementations should provide the logic to determine if the game has been lost
     * based on specific criteria.
     * </p>
     */
    void checkIfLost();
}