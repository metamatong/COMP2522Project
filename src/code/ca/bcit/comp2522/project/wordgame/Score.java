package ca.bcit.comp2522.project.wordgame;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

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
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private LocalDateTime currentTime;
    private int numGamesPlayed;
    private int numCorrectFirstAttempts;
    private int numCorrectSecondAttempts;
    private int numIncorrectTwoAttempts;

    /**
     * Constructs a Score object with the specified values.
     *
     * @param currentTime             the date and time when this score was recorded
     * @param numGamesPlayed          the total number of games played
     * @param numCorrectFirstAttempts the number of questions answered correctly on the first attempt
     * @param numCorrectSecondAttempts the number of questions answered correctly on the second attempt
     * @param numIncorrectTwoAttempts  the number of questions answered incorrectly after two attempts
     */
    public Score(final LocalDateTime currentTime,
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
    public double getAverageScore()
    {
        final double averageScore;
        averageScore = (double) this.getScore() / numGamesPlayed;
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

    /**
     * Returns a string representation of this Score.
     * <p>
     * The format is:
     * Date and Time: {@value #DATE_FORMAT}
     * Games Played: [number]
     * Correct First Attempts: [number]
     * Correct Second Attempts: [number]
     * Incorrect Attempts: [number]
     * Score: [total score] points
     * </p>
     *
     * @return a formatted string of the score details.
     */
    @Override
    public String toString()
    {
        final DateTimeFormatter formatter;
        formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        return String.format("Date and Time: %s\n" +
                             "Games Played: %d\n" +
                             "Correct First Attempts: %d\n" +
                             "Correct Second Attempts: %d\n" +
                             "Incorrect Attempts: %d\n" +
                             "Score: %d points\n",
                             currentTime.format(formatter),
                             numGamesPlayed,
                             numCorrectFirstAttempts,
                             numCorrectSecondAttempts,
                             numIncorrectTwoAttempts,
                             getScore()
        );
    }

    /**
     * Appends the score details to the specified file.
     * <p>
     * The score is appended in a human-readable format, including the date/time,
     * games played, correct attempts, incorrect attempts, and total score.
     * </p>
     *
     * @param score         the Score object containing the score details
     * @param scoreFilePath the file path to which the score will be appended
     */
    public static void appendScoreToFile(final Score score,
                                         final String scoreFilePath)
    {
        final DateTimeFormatter formatter;
        final String formattedDateTime;

        formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        formattedDateTime = score.currentTime.format(formatter);

        // Create a File object and ensure the file exists.
        final File file;
        file = new File(scoreFilePath);
        try
        {
            if(!file.exists())
            {
                file.createNewFile();
            }
        }
        catch(final IOException e)
        {
            e.printStackTrace();
        }

        try(final FileWriter writer = new FileWriter(scoreFilePath, true))
        {
            writer.write(System.lineSeparator());
            writer.write("Date and Time: " + formattedDateTime + "\n");
            writer.write("Games Played: " + score.numGamesPlayed + "\n");
            writer.write("Correct First Attempts: " + score.numCorrectFirstAttempts + "\n");
            writer.write("Correct Second Attempts: " + score.numCorrectSecondAttempts + "\n");
            writer.write("Incorrect Attempts: " + score.numIncorrectTwoAttempts + "\n");
            writer.write("Total Score: " + score.getScore() + " points\n");
        }
        catch(final IOException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Reads the score details from the specified file (given by its path) and returns them as a list of Score objects.
     * <p>
     * The file is expected to contain scores in the following format (one record per block):
     * <pre>
     * Date and Time: {@value #DATE_FORMAT}
     * Games Played: [number]
     * Correct First Attempts: [number]
     * Correct Second Attempts: [number]
     * Incorrect Attempts: [number]
     * Total Score: [number] points
     * </pre>
     * Blank lines between records are ignored.
     * </p>
     *
     * @param scoreFilePath the path to the file from which to read the scores
     * @return a list of Score objects read from the file
     */
    public static List<Score> readScoresFromFile(final String scoreFilePath)
    {
        final List<Score> scoresList;
        final DateTimeFormatter formatter;
        final File file;

        scoresList = new ArrayList<>();
        formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        file = new File(scoreFilePath);

        if(!file.exists())
        {
            System.out.println("Score file not found at " + scoreFilePath + ". Returning an empty list.");
            return scoresList;
        }

        try(final BufferedReader reader = new BufferedReader(new FileReader(scoreFilePath)))
        {
            String line;
            while((line = reader.readLine()) != null)
            {
                if(line.trim().isEmpty())
                {
                    continue;
                }
                // Expecting the first line of a record to start with "Date and Time: "
                if(!line.startsWith("Date and Time: "))
                {
                    continue;
                }

                final String dateTimeString;
                final LocalDateTime dateTime;

                dateTimeString = line.substring("Date and Time: ".length()).trim();
                dateTime = LocalDateTime.parse(dateTimeString, formatter);

                line = reader.readLine();
                final int gamesPlayed;
                gamesPlayed = Integer.parseInt(line.substring("Games Played: ".length()).trim());

                line = reader.readLine();
                final int correctFirst;
                correctFirst = Integer.parseInt(line.substring("Correct First Attempts: ".length()).trim());

                line = reader.readLine();
                final int correctSecond;
                correctSecond = Integer.parseInt(line.substring("Correct Second Attempts: ".length()).trim());

                line = reader.readLine();
                final int incorrectAttempts;
                incorrectAttempts = Integer.parseInt(line.substring("Incorrect Attempts: ".length()).trim());

                reader.readLine();

                final Score score;
                score = new Score(dateTime, gamesPlayed, correctFirst, correctSecond, incorrectAttempts);
                scoresList.add(score);
            }
        }
        catch(final IOException e)
        {
            e.printStackTrace();
        }
        return scoresList;
    }
}
