package ca.bcit.comp2522.project;

import java.time.LocalDateTime;


/**
 * Represents the score details of a word game session.
 * This class tracks the date/time the session was played, how many games
 * were played, the number of correct attempts (first and second),
 * and the number of incorrect attempts. It also provides a method
 * to calculate an average score based on these details.
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class Score
{
    private static final int WEIGHT_FOR_FIRST_CORRECT_ATTEMPTS = 2;

    private LocalDateTime currentTime;
    private int numGamesPlayed;
    private int numCorrectFirstAttempts;
    private int numCorrectSecondAttempts;
    private int numIncorrectTwoAttempts;

    /*
     * Constructs a Score object with the specified values.
     *
     * @param currentTime             the date and time when this score was recorded
     * @param numGamesPlayed          the total number of games played
     * @param numCorrectFirstAttempts the number of questions answered correctly on the first attempt
     * @param numCorrectSecondAttempts the number of questions answered correctly on the second attempt
     * @param numIncorrectTwoAttempts  the number of questions answered incorrectly after two attempts
     */
    Score(final LocalDateTime currentTime,
          final int numGamesPlayed,
          final int numCorrectFirstAttempts,
          final int numCorrectSecondAttempts,
          final int numIncorrectTwoAttempts)
    {
        this.currentTime = currentTime;
        this.numGamesPlayed = numGamesPlayed;
        this.numCorrectFirstAttempts = numCorrectFirstAttempts;
        this.numCorrectSecondAttempts = numCorrectSecondAttempts;
        this.numIncorrectTwoAttempts = numIncorrectTwoAttempts;
    }

    /**
     * Calculates and returns the total score based on the current
     * session data. The calculation uses a weighting factor for first
     * attempts.
     * <p>
     * The formula is:
     * WEIGHT_FOR_FIRST_CORRECT_ATTEMPTS * numCorrectFirstAttempts + numCorrectSecondAttempts
     *
     *
     * @return the total score for this session
     */
    public int getScore()
    {
        final int totalScore;
        totalScore = WEIGHT_FOR_FIRST_CORRECT_ATTEMPTS * numCorrectFirstAttempts + numCorrectSecondAttempts;
        return totalScore;
    }

    /**
     * Calculates and returns the average score based on the current
     * session data. The calculation divides total score by the total number of games played.
     * <p>
     * The formula is:
     * totalScore / numGamesPlayed
     *
     * @return the average score for this session
     */
    public int getAverageScore()
    {
        final int averageScore;
        averageScore = this.getScore() / numGamesPlayed;
        return averageScore;
    }

    /**
     * Returns the local date time stored in the Score object.
     *
     * @return the local date time for this object
     */
    public LocalDateTime getDateTime()
    {
        return this.currentTime;
    }
}
