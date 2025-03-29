package ca.bcit.comp2522.project.numbergame;

/**
 * Abstract class that provides a basic scoreboard implementation.
 *
 * @author Kyle Cheon
 * @version 1.0
 */
abstract class AbstractNumberGame implements NumberGameStats
{
    private int totalGamesPlayed;
    private int totalWins;
    private int totalLosses;
    private int totalPlacements;

    /**
     * Returns the total number of games played.
     *
     * @return the total games played
     */
    @Override
    public int getTotalGamesPlayed()
    {
        return totalGamesPlayed;
    }

    /**
     * Returns the total number of wins.
     *
     * @return the total wins
     */
    @Override
    public int getTotalWins()
    {
        return totalWins;
    }

    /**
     * Returns the total number of losses.
     *
     * @return the total losses
     */
    @Override
    public int getTotalLosses()
    {
        return totalLosses;
    }

    /**
     * Returns the total number of placements.
     *
     * @return the total placements
     */
    @Override
    public int getTotalPlacements()
    {
        return totalPlacements;
    }

    /**
     * Calculates and returns the average number of placements per game.
     * <p>
     * If no games have been played, the method returns 0.0.
     * </p>
     *
     * @return the average placements per game
     */
    @Override
    public double getAveragePlacementsPerGame()
    {
        if(totalGamesPlayed == 0)
        {
            return 0.0;
        }
        return (double) totalPlacements / (double) totalGamesPlayed;
    }

    /**
     * Increments the count of games played by one.
     */
    @Override
    public void incrementGamesPlayed()
    {
        totalGamesPlayed++;
    }

    /**
     * Increments the count of wins by one.
     */
    @Override
    public void incrementWins()
    {
        totalWins++;
    }

    /**
     * Increments the count of losses by one.
     */
    @Override
    public void incrementLosses()
    {
        totalLosses++;
    }

    /**
     * Adds the number of placements from the current game to the total placements.
     *
     * @param placementsThisGame the placements achieved in the current game
     */
    @Override
    public void addToTotalPlacements(final int placementsThisGame)
    {
        totalPlacements += placementsThisGame;
    }

    /**
     * Returns a summary of the scoreboard including games played, wins, losses,
     * total placements, and the average placements per game.
     *
     * @return a formatted summary string of the scoreboard
     */
    @Override
    public String getScoreboardSummary()
    {
        final StringBuilder sb;
        sb = new StringBuilder();
        sb.append("Games played: ").append(totalGamesPlayed).append("\n");
        sb.append("Wins: ").append(totalWins).append("\n");
        sb.append("Losses: ").append(totalLosses).append("\n");
        sb.append("Total placements: ").append(totalPlacements).append("\n");
        sb.append("Average placements per game: ")
                .append(String.format("%.2f", getAveragePlacementsPerGame()));
        return sb.toString();
    }

    /**
     * Resets the game to its initial state.
     * <p>
     * This abstract method should be implemented by subclasses to reset any game-specific data.
     * </p>
     */
    public abstract void resetGame();

    /**
     * Checks if the game is lost and handles the loss condition.
     * <p>
     * This abstract method should be implemented by subclasses to determine if the game
     * has been lost based on specific criteria and to handle any associated logic.
     * </p>
     */
    public abstract void checkIfLost();
}