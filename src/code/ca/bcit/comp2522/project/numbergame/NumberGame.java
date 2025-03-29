package ca.bcit.comp2522.project.numbergame;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.Arrays;

/**
 * Concrete NumberGame that uses a BasicScoreboard for tracking stats and runs a JavaFX GUI.
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class NumberGame extends Application
        implements Game, javafx.event.EventHandler<javafx.event.ActionEvent>
{
    private CountDownLatch gameLatch;
    private static final int ROWS = 4;
    private static final int COLS = 5;
    private static final int TOTAL_NUMBERS_TO_PLACE = 20;
    private static final int NUMBER_UPPERBOUND = 1000;
    private static final int ZERO_VALUE = 0;
    private static final int SCENE_WIDTH = 400;
    private static final int SCENE_HEIGTH = 260;
    private static final int HBOX_HEIGHT = 20;
    private static final int VBOX_WIDTH = 10;
    private static final int BUTTON_MIN_WIDTH = 60;
    private static final int BUTTON_MIN_HEIGHT = 40;
    private Stage primaryStage;

    // Embedded scoreboard
    private BasicScoreboard scoreboard;

    // UI components
    private final Button[][] buttons = new Button[ROWS][COLS];
    private Label nextNumberLabel;
    private Label statusLabel;

    // Board data: 20 slots, 0 means "empty"
    private final int[] board = new int[ROWS * COLS];

    // Current game state
    private final Random random = new Random();
    private int nextNumber;      // The next random number to place
    private int numbersPlaced;   // How many we've successfully placed so far
    private boolean gameOver;    // true if player has won or lost already

    /**
     * Sets up and displays the JavaFX UI for the Number Game.
     *
     * @param primaryStage the main Stage for this JavaFX application
     */
    @Override
    public void start(final Stage primaryStage)
    {
        this.primaryStage = primaryStage;
        scoreboard = new BasicScoreboard();

        // If the user clicks the [X] to close the window:
        primaryStage.setOnCloseRequest(e -> {
            e.consume();          // prevent default close
            closeGameWindow();    // custom method
        });


        final GridPane gridPane;
        gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.setMaxHeight(Double.MAX_VALUE);

        IntStream.range(0, COLS).forEach(i ->
        {
            final ColumnConstraints cc;
            cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / COLS);
            gridPane.getColumnConstraints().add(cc);
        });

        IntStream.range(0, ROWS).forEach(i ->
        {
            final RowConstraints rc;
            rc = new RowConstraints();
            rc.setPercentHeight(100.0 / ROWS);
            gridPane.getRowConstraints().add(rc);
        });

        IntStream.range(0, ROWS).forEach(r ->
                IntStream.range(0, COLS).forEach(c ->
                {
                    Button b = new Button("");
                    b.setMinWidth(BUTTON_MIN_WIDTH);
                    b.setMinHeight(BUTTON_MIN_HEIGHT);
                    b.setOnAction(this);
                    gridPane.add(b, c, r);
                    buttons[r][c] = b;
                })
        );

        nextNumberLabel = new Label("Next number: ");
        statusLabel = new Label("Status: Playing...");

        final VBox topBar;
        topBar = new VBox(VBOX_WIDTH, nextNumberLabel, statusLabel);
        final HBox bottomBar;
        bottomBar = new HBox(HBOX_HEIGHT);

        final VBox root;
        root = new VBox(VBOX_WIDTH, topBar, gridPane, bottomBar);
        root.setFillWidth(true);

        final Scene scene;
        scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGTH);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );

        primaryStage.setTitle("Number Game");
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start the first game
        resetGame();
    }

    /**
     * Starts the Number Game by creating a new JavaFX Stage and initializing the game UI.
     * <p>
     * This method assigns the provided {@code CountDownLatch} to the gameLatch field so that the game
     * can signal its completion by counting down the latch when the game is over. A new stage is then
     * created and passed to the {@link #start(Stage)} method to launch the game.
     * </p>
     *
     * @param latch the {@link CountDownLatch} used to signal when the game is complete
     */
    @Override
    public void play(final CountDownLatch latch)
    {
        this.gameLatch = latch;
        final Stage stage;
        stage = new Stage();
        start(stage);
    }

    // ---------------------
    // Scoreboard Delegation
    // ---------------------

    /**
     * Increments the count of games played by 1 in the scoreboard.
     */
    public void incrementGamesPlayed()
    {
        scoreboard.incrementGamesPlayed();
    }

    /**
     * Increments the count of wins by 1 in the scoreboard.
     */
    public void incrementWins()
    {
        scoreboard.incrementWins();
    }

    /**
     * Increments the count of losses by 1 in the scoreboard.
     */
    public void incrementLosses()
    {
        scoreboard.incrementLosses();
    }

    /**
     * Adds a given number to the scoreboard's total placements.
     *
     * @param placementsThisGame the number of placements to add
     */
    public void addToTotalPlacements(final int placementsThisGame)
    {
        scoreboard.addToTotalPlacements(placementsThisGame);
    }

    /**
     * Retrieves a summary of the scoreboard's statistics.
     *
     * @return a string representing the scoreboard data
     */
    public String getScoreboardSummary()
    {
        return scoreboard.getScoreboardSummary();
    }

    // -------------
    // Game Methods
    // -------------

    /**
     * Resets the game to a clean state:
     * <ul>
     *     <li>Clears the board array.</li>
     *     <li>Clears all button labels.</li>
     *     <li>Resets relevant counters.</li>
     *     <li>Generates a fresh random number to place first.</li>
     *     <li>Increments 'games played' in the scoreboard.</li>
     * </ul>
     */
    public void resetGame() {
        // Clear the board array
        Arrays.fill(board, ZERO_VALUE);

        // Clear all button labels
        Arrays.stream(buttons)
                .forEach(row -> Arrays.stream(row)
                        .forEach(button -> button.setText("")));

        numbersPlaced = ZERO_VALUE;
        gameOver = false;

        statusLabel.setText("Status: Playing...");

        // Start a new random number
        nextNumber = getRandomNumber();
        updateNextNumberLabel();

        // We have not started a new official game until now, so increment
        incrementGamesPlayed();
    }

    /**
     * Main event handler for the grid buttons. Places the next number if valid,
     * checks for ascending order, and updates the UI. If an invalid move is made,
     * it will signal a loss. If the board is filled, it signals a win.
     *
     * @param event the event triggered by clicking on a grid Button.
     */
    @Override
    public void handle(final ActionEvent event)
    {
        if(gameOver)
        {
            return;
        }

        final Button clicked;
        clicked = (Button) event.getSource();

        // Find row & column
        int rowIndex = -1;
        int colIndex = -1;
        outerLoop:
        for(int r = 0; r < ROWS; r++)
        {
            for(int c = 0; c < COLS; c++)
            {
                if(buttons[r][c] == clicked)
                {
                    rowIndex = r;
                    colIndex = c;
                    break outerLoop;
                }
            }
        }

        int boardIndex = rowIndex * COLS + colIndex;
        if(board[boardIndex] != ZERO_VALUE)
        {
            // Already has a number, ignore
            return;
        }

        // Tentatively place the number
        board[boardIndex] = nextNumber;

        // Check if still ascending
        if(!isBoardAscending(board))
        {
            // When the game is over due to an invalid move:
            gameOver = true;
            board[boardIndex] = ZERO_VALUE; // revert the move
            incrementLosses();
            addToTotalPlacements(numbersPlaced);
            statusLabel.setText("Status: You lose! No place for " + nextNumber);
            showGameOverDialog(primaryStage);
            return;
        }

        // If valid, show on UI
        clicked.setText(String.valueOf(nextNumber));
        numbersPlaced++;

        // Win condition: all 20 placed
        if(numbersPlaced == TOTAL_NUMBERS_TO_PLACE)
        {
            gameOver = true;
            incrementWins();
            addToTotalPlacements(numbersPlaced);
            statusLabel.setText("Status: You win! Placed all 20 numbers.");
            return;
        }

        // Otherwise, get the next random number
        nextNumber = getRandomNumber();

        // Check if we can place nextNumber at all
        if(!canPlaceNextNumber(nextNumber, board))
        {
            gameOver = true;
            incrementLosses();
            addToTotalPlacements(numbersPlaced);
            statusLabel.setText("Status: You lose! Next was " + nextNumber
                    + "\n but there's no valid spot.");
            showGameOverDialog(primaryStage);
            return;
        }

        // If we can continue, show the next number
        updateNextNumberLabel();
    }

    // ----------------
    // Helper Utilities
    // ----------------

    /*
     * Generates a random integer in the range [1, NUMBER_UPPERBOUND].
     *
     * @return a random integer from 1 to NUMBER_UPPERBOUND (inclusive)
     */
    private int getRandomNumber()
    {
        return random.nextInt(NUMBER_UPPERBOUND) + 1;
    }

    /*
     * Updates the nextNumberLabel to display the nextNumber.
     */
    private void updateNextNumberLabel()
    {
        nextNumberLabel.setText("Next number: " + nextNumber);
    }

    /*
     * Checks whether the given array of integers (representing the board)
     * is in strictly ascending order, ignoring zeroes.
     *
     * @param arr the board array to check
     * @return true if all non-zero values are in strictly ascending order; false otherwise
     */
    private boolean isBoardAscending(final int[] arr)
    {
        int[] nonZero = Arrays.stream(arr)
                .filter(v -> v != 0)
                .toArray();
        return IntStream.range(1, nonZero.length)
                .allMatch(i -> nonZero[i] > nonZero[i - 1]);
    }

    /*
     * Tries placing a hypothetical number into each empty spot. If at least one
     * placement leads to a strictly ascending board, then it's possible to place
     * this number.
     *
     * @param n   the candidate number to place
     * @param arr the board array
     * @return true if there is at least one valid spot; false otherwise
     */
    private boolean canPlaceNextNumber(final int n, final int[] arr)
    {
        return IntStream.range(0, arr.length).anyMatch(i ->
        {
            if(arr[i] == ZERO_VALUE)
            {
                arr[i] = n;
                final boolean ascending;
                ascending = isBoardAscending(arr);
                arr[i] = ZERO_VALUE;
                return ascending;
            }
            return false;
        });
    }

    /*
     * Shows a dialog indicating that the game is over, then asks the user if they want
     * to reset or quit. If the user chooses "Try Again," a new game is started via
     * resetGame(); otherwise, the application window is closed.
     *
     * @param stage the main stage on which to show the dialog
     */
    private void showGameOverDialog(final Stage stage)
    {
        final Alert gameOverAlert;
        gameOverAlert = new Alert(Alert.AlertType.CONFIRMATION);
        gameOverAlert.setTitle("Game Over");
        gameOverAlert.setHeaderText("Game Over");
        gameOverAlert.setContentText(statusLabel.getText() + "\nWould you like to try again or quit?");
        final ButtonType tryAgainOption;
        tryAgainOption = new ButtonType("Try Again");
        final ButtonType quitOption;
        quitOption = new ButtonType("Quit");
        gameOverAlert.getButtonTypes().setAll(tryAgainOption, quitOption);

        final Optional<ButtonType> result;
        result = gameOverAlert.showAndWait();
        if(result.isPresent() && result.get() == tryAgainOption)
        {
            resetGame();
        }
        else
        {
            stage.close();
        }
    }


    /*
     * Closes the game window and signals that the game is over.
     * <p>
     * If a {@code CountDownLatch} is present, its count is decremented to indicate game completion.
     * Then, if the primary stage exists, it is closed. This method is typically called when the user
     * chooses to exit the game.
     * </p>
     */
    private void closeGameWindow() {
        if (gameLatch != null) {
            gameLatch.countDown();
        }
        if (primaryStage != null) {
            primaryStage.close();
        }
    }
}