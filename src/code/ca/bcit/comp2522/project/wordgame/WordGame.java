package ca.bcit.comp2522.project.wordgame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Implements a word-based game that tests the user's knowledge of countries, their capitals, and related facts.
 * <p>
 * In this game, the user is presented with a series of questions (typically 10 per session) based on one of three game
 * types:
 * <ul>
 *   <li><b>Capital to Country:</b> The question provides a capital city and the user must respond with the correct
 *   country.</li>
 *   <li><b>Country to Capital:</b> The question provides a country name and the user must identify its capital.</li>
 *   <li><b>Fact to Country:</b> The question presents a fact about a country, and the user answers with the country's
 *   name.</li>
 * </ul>
 * After each set of questions, the session statistics are displayed (including counts for correct answers on the first
 * or second attempt and incorrect attempts) and the user is prompted to play again. When the user quits, the session's
 * score is saved to a file.
 * </p>
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class WordGame
{
    private static final int NUMBER_OF_QUESTIONS = 10;
    private static final int INITIAL_HIGH_SCORE = 0;
    private static final int INITIAL_AVERAGE_SCORE = 0;

    private final World world;
    private int playedGameNumber = 0;
    private int firstAttemptCorrectNumber = 0;
    private int secondAttemptCorrectNumber = 0;
    private int thirdAttemptNumber = 0;

    /**
     * Constructs a new {@code WordGame} and initializes the game world.
     * <p>
     * The constructor creates a new {@link World} object that encapsulates all available country data,
     * including country names, capital cities, and related facts. This world is used throughout the game to
     * retrieve random questions.
     * </p>
     */
    public WordGame()
    {
        final World world;
        world = new World();
        this.world = world;
    }

    /**
     * Starts the word game session.
     * <p>
     * This method initiates the game loop, which repeatedly asks a fixed number of questions (as defined by
     * {@code NUMBER_OF_QUESTIONS}). For each question, the method:
     * <ul>
     *   <li>Fetches a question/answer pair based on a randomly selected game type.</li>
     *   <li>Prompts the user to input their answer, first attempting to answer correctly.</li>
     *   <li>If the first attempt is incorrect, the user is given a second attempt; if this is also incorrect,
     *       the correct answer is displayed and the incorrect counter is incremented.</li>
     * </ul>
     * After processing all questions, the game session statistics are displayed, and the user is prompted to
     * play again. If the user opts to stop playing, the session score is saved to a file.
     * </p>
     */
    public void playWordGame()
    {
        System.out.println("Welcome to WordGame!");
        System.out.println("Let's start the Word Game right away!");
        final Scanner scanner;
        scanner = new Scanner(System.in);

        while (true)
        {
            for(int i = 0; i < NUMBER_OF_QUESTIONS; i++)
            {
                final String[] questionAndAnswer;
                questionAndAnswer = getWordGameQuestionAndAnswer();

                final String question;
                final String answer;

                if(questionAndAnswer != null)
                {
                    question = questionAndAnswer[0];
                    answer = questionAndAnswer[1];

                    System.out.println("Question " + (i + 1) + ": ");
                    System.out.println(question);
                    System.out.println("Enter your answer: ");
                    String inputAnswer = scanner.nextLine().trim();

                    if(inputAnswer.equalsIgnoreCase(answer))
                    {
                        System.out.println("CORRECT");
                        firstAttemptCorrectNumber++;
                    }
                    else
                    {
                        System.out.println("INCORRECT");
                        System.out.println("Try again: ");
                        inputAnswer = scanner.nextLine().trim();
                        if(inputAnswer.equalsIgnoreCase(answer))
                        {
                            System.out.println("CORRECT");
                            secondAttemptCorrectNumber++;
                        }
                        else
                        {
                            System.out.println("INCORRECT");
                            System.out.println("The correct answer was " + answer);
                            thirdAttemptNumber++;
                        }
                    }
                }
                else
                {
                    System.out.println("Failed to fetch question.");
                }
            }
            playedGameNumber++;
            System.out.println(playedGameNumber + " word game played.");
            System.out.println(firstAttemptCorrectNumber + " correct answers on the first attempt.");
            System.out.println(secondAttemptCorrectNumber + " correct answers on the second attempt.");
            System.out.println(thirdAttemptNumber + " incorrect answers on two attempts each.");

            boolean validResponse = false;
            while(!validResponse)
            {
                System.out.println("Do you want to play again? (yes/no):");
                final String inputToPlayAgain;
                inputToPlayAgain = scanner.nextLine();
                final String normalizedInput;
                normalizedInput = inputToPlayAgain.trim().toLowerCase();

                switch(normalizedInput)
                {
                    case "yes":
                        validResponse = true;
                        break;
                    case "no":
                        saveScoresToFile();
                        return;
                    default:
                        System.out.println("Wrong input. Try again. (Yes or No)");
                        break;
                }
            }
        }
    }

    /*
     * Retrieves a random question and its corresponding answer.
     * <p>
     * This helper method selects a random game type from the {@code WordGameType} enum and delegates
     * to the appropriate method to fetch a question/answer pair. The game types include:
     * <ul>
     *   <li><b>Capital to Country</b>: Uses the capital city as the prompt and expects the country name as the answer.
     * </li>
     *   <li><b>Country to Capital</b>: Uses the country name as the prompt and expects the capital city as the answer.
     * </li>
     *   <li><b>Fact to Country</b>: Uses a factual statement about a country as the prompt and expects the country
     *   name as the answer.</li>
     * </ul>
     * </p>
     *
     * @return a String array where the first element is the question and the second element is the answer,
     *         or {@code null} if no valid question can be generated.
     */
    private String[] getWordGameQuestionAndAnswer()
    {
        // Using WordGameType enum for choosing game type.
        final WordGameType[] gameTypes;
        final Random random;
        final int randomIndex;

        random = new Random();
        gameTypes = WordGameType.values();
        randomIndex = random.nextInt(gameTypes.length);

        // Select a random game type
        final WordGameType randomGame;
        randomGame = gameTypes[randomIndex];

        final String[] questionAndAnswerPair;
        questionAndAnswerPair = switch(randomGame)
        {
            case CAPITAL_TO_COUNTRY ->
            {
                System.out.println("Question Type: Capital to Country");
                yield fetchCapitalQuestionAndAnswer();
            }
            case COUNTRY_TO_CAPITAL ->
            {
                System.out.println("Question Type: Country to Capital");
                yield fetchCountryQuestionAndAnswer();
            }
            case FACT_TO_COUNTRY ->
            {
                System.out.println("Question Type: Fact to Country");
                yield fetchFactQuestionAndAnswer();
            }
            default ->
            {
                System.out.println("Question Type: Error. No such word game exists.");
                yield null;
            }
        };
        return questionAndAnswerPair;
    }

    /*
     * Fetches a question/answer pair for the "Capital to Country" game type.
     * <p>
     * In this game type, the prompt is the capital city name and the answer is the corresponding country name.
     * A random country is selected from the game world for this purpose.
     * </p>
     *
     * @return a String array where the first element is the capital city (question) and the second element is the
     * country name (answer).
     */
    private String[] fetchCapitalQuestionAndAnswer()
    {
        final Map<String, Country> countries;
        // We change HashMap into a List to randomly select any Country object.
        final List<Country> countriesList;
        final Random random;
        final int randomIndex;
        final Country randomCountry;
        final String question;
        final String answer;

        countries = world.getCountries();
        countriesList = new ArrayList<>(countries.values());
        random = new Random();
        randomIndex = random.nextInt(countriesList.size());
        randomCountry = countriesList.get(randomIndex);
        question = randomCountry.getCapitalCityName();
        answer = randomCountry.getCountryName();

        return new String[] { question, answer };
    }

    /*
     * Fetches a question/answer pair for the "Country to Capital" game type.
     * <p>
     * In this game type, the prompt is the country name and the answer is its corresponding capital city.
     * A random country is selected from the game world to generate the question.
     * </p>
     *
     * @return a String array where the first element is the country name (question) and the second element is the
     * capital city (answer).
     */
    private String[] fetchCountryQuestionAndAnswer()
    {
        final Map<String, Country> countries;
        // We change HashMap into a List to randomly select any Country object.
        final List<Country> countriesList;
        final Random random;
        final int randomIndex;
        final Country randomCountry;
        final String question;
        final String answer;

        countries = world.getCountries();
        countriesList = new ArrayList<>(countries.values());
        random = new Random();
        randomIndex = random.nextInt(countriesList.size());
        randomCountry = countriesList.get(randomIndex);
        question = randomCountry.getCountryName();
        answer = randomCountry.getCapitalCityName();

        return new String[] { question, answer };
    }

    /*
     * Fetches a question/answer pair for the "Fact to Country" game type.
     * <p>
     * In this game type, the prompt is a randomly selected fact about a country,
     * and the answer is the name of that country. A random country and a random fact about that country are chosen.
     * </p>
     *
     * @return a String array where the first element is a fact (the question) and the second element is the country
     * name (the answer).
     */
    private String[] fetchFactQuestionAndAnswer()
    {
        final Map<String, Country> countries;
        // We change HashMap into a List to randomly select any Country object.
        final List<Country> countriesList;
        final Random random;
        final int randomIndex;
        final Country randomCountry;
        final int randomIndexForFact;
        final String question;
        final String answer;

        countries = world.getCountries();
        countriesList = new ArrayList<>(countries.values());
        random = new Random();
        randomIndex = random.nextInt(countriesList.size());
        randomCountry = countriesList.get(randomIndex);
        randomIndexForFact = random.nextInt(randomCountry.getFactsArray().length);
        question = randomCountry.getFactsArray()[randomIndexForFact];
        answer = randomCountry.getCountryName();

        return new String[] { question, answer };
    }


    /*
     * Saves the current game session's scores to a file.
     * <p>
     * This method creates a {@link Score} object using the current game statistics and the current date/time.
     * It then reads previously stored scores to determine the current high score and reports the scores via a
     * dedicated method. Finally, it appends the current score to the score file for persistent storage.
     * </p>
     */
    private void saveScoresToFile()
    {
        final String scoreFileUrl;
        final LocalDateTime currentTime;
        final Score currentScore;

        scoreFileUrl = "src/res/scores/scores.txt";
        currentTime = LocalDateTime.now();
        currentScore = new Score(currentTime,
                                 playedGameNumber,
                                 firstAttemptCorrectNumber,
                                 secondAttemptCorrectNumber,
                                 thirdAttemptNumber);


        double previousHighestScore;
        previousHighestScore = INITIAL_HIGH_SCORE;

        LocalDateTime previousHighestScoreTime;
        previousHighestScoreTime = LocalDateTime.now();

        final List<Score> scores;
        scores = Score.readScoresFromFile(scoreFileUrl);

        for(final Score score: scores)
        {
            final double averageHighScore;
            averageHighScore = score.getAverageScore();

            if(averageHighScore > previousHighestScore)
            {
                previousHighestScore = averageHighScore;
                previousHighestScoreTime = score.getDateTime();
            }
        }

        reportScores(currentScore, previousHighestScore, previousHighestScoreTime);

        // This must be done before calculating previousHighestScore
        Score.appendScoreToFile(currentScore, scoreFileUrl);
    }

    /*
     * Reports the current game session's score and the previous high score details to the user.
     * <p>
     * This method formats the previous high score date and time using specific patterns and then prints a message:
     * <ul>
     *   <li>If the current session is the player's first game (score of 0), a first-time message is shown.</li>
     *   <li>If the current session's average score is higher than the previous high score, a congratulatory message
     *   is displayed.</li>
     *   <li>Otherwise, the user is informed that they did not beat the high score.</li>
     * </ul>
     * </p>
     *
     * @param currentScore           the {@link Score} object of the current game session.
     * @param previousHighestScore   the previous highest average score.
     * @param previousHighestScoreTime the date and time when the previous high score was achieved.
     */
    private void reportScores(final Score currentScore,
                              final double previousHighestScore,
                              final LocalDateTime previousHighestScoreTime)
    {
        final DateTimeFormatter formatterForDate;
        final DateTimeFormatter formatterForTime;
        final String formattedDate;
        final String formattedTime;

        formatterForDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        formatterForTime = DateTimeFormatter.ofPattern("HH:mm:ss");
        formattedDate = previousHighestScoreTime.format(formatterForDate);
        formattedTime = previousHighestScoreTime.format(formatterForTime);

        if(currentScore.getAverageScore() == INITIAL_AVERAGE_SCORE)
        {
            System.out.println("It's your first time! The score is 0.");
        }
        else
        {
            if(currentScore.getAverageScore() > previousHighestScore)
            {
                System.out.println("CONGRATULATIONS!\nYou are the new high score with an average of " +
                        currentScore.getAverageScore() +
                        " points per game;\n the previous record was " +
                        previousHighestScore +
                        " points per game on " +
                        formattedDate +
                        " at " +
                        formattedTime);
            }
            else
            {
                System.out.println("You did not beat the high score of " +
                        previousHighestScore +
                        " points per game from " +
                        formattedDate +
                        " at " +
                        formattedTime);
            }
        }
    }
}
