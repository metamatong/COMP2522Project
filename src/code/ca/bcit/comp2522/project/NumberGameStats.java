package ca.bcit.comp2522.project;

/**
 * Interface providing methods for game stats.
 *
 * @author Kyle Cheon
 * @version 1.0
 */
interface NumberGameStats
{
    int getTotalGamesPlayed();
    int getTotalWins();
    int getTotalLosses();
    int getTotalPlacements();
    double getAveragePlacementsPerGame();

    void incrementGamesPlayed();
    void incrementWins();
    void incrementLosses();
    void addToTotalPlacements(int placementsThisGame);

    String getScoreboardSummary();

    void resetGame();

    void checkIfLost();
}