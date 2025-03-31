package ca.bcit.comp2522.project.wordgame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * The class that implements a word-based game that tests the user's knowledge
 * of countries, their capitals, and related facts.
 * The game continues for a set number of questions per session and then prompts the user to play again.
 * Scores are saved to a file after the user decides to quit.
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
     * Constructs a new WordGame and initializes the game world and score list.
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
     * The method repeatedly asks a set number of questions to the user,
     * checks their answers, and displays the current score tally.
     * Once the session is over, the user is prompted whether to play again.
     * If the user chooses not to continue, scores are saved to file.
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
     * This method randomly selects a game type from the WordGameType enum and
     * delegates to the appropriate method to fetch the question/answer pair.
     * </p>
     *
     * @return a String array where the first element is the question and the second element is the answer,
     *         or null if no valid question is available.
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
     * Fetches a question and answer pair for the "Capital to Country" game type.
     * <p>
     * The question is the capital city name, and the answer is the corresponding country name.
     * </p>
     *
     * @return a String array where the first element is the capital city (question) and the
     *         second element is the country name (answer).
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
     * Fetches a question and answer pair for the "Country to Capital" game type.
     * <p>
     * The question is the country name, and the answer is the corresponding capital city.
     * </p>
     *
     * @return a String array where the first element is the country name (question) and the
     *         second element is the capital city name (answer).
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
     * Fetches a question and answer pair for the "Fact to Country" game type.
     * <p>
     * The question is a fact about a country, and the answer is the country's name.
     * </p>
     *
     * @return a String array where the first element is a fact (question) and the second element is the
     *         country name (answer).
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
     * Saves the current game scores to a file.
     * <p>
     * This method writes the game details, including the date/time, number of questions played,
     * correct and incorrect answers, and total score to a designated file. It then determines
     * the previous high score and delegates reporting to reportScores(Score, int, LocalDateTime).
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
     * Reports the current game session's score and the high score details to the user.
     *
     * @param currentScore the score of the current game session.
     * @param previousHighestScore the previous highest average score.
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
                System.out.println("CONGRATULATIONS! You are the new high score with an average of " +
                        currentScore.getAverageScore() +
                        " points per game; the previous record was " +
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
