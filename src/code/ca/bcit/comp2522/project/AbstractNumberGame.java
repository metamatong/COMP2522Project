package ca.bcit.comp2522.project;

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

    @Override
    public int getTotalGamesPlayed()
    {
        return totalGamesPlayed;
    }

    @Override
    public int getTotalWins()
    {
        return totalWins;
    }

    @Override
    public int getTotalLosses()
    {
        return totalLosses;
    }

    @Override
    public int getTotalPlacements()
    {
        return totalPlacements;
    }

    @Override
    public double getAveragePlacementsPerGame()
    {
        if(totalGamesPlayed == 0)
        {
            return 0.0;
        }
        return (double) totalPlacements / (double) totalGamesPlayed;
    }

    @Override
    public void incrementGamesPlayed()
    {
        totalGamesPlayed++;
    }

    @Override
    public void incrementWins()
    {
        totalWins++;
    }

    @Override
    public void incrementLosses()
    {
        totalLosses++;
    }

    @Override
    public void addToTotalPlacements(final int placementsThisGame)
    {
        totalPlacements += placementsThisGame;
    }

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

    public abstract void resetGame();
    public abstract void checkIfLost();
}