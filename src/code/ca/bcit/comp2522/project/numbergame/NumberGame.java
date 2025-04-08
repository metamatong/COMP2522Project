package ca.bcit.comp2522.project.numbergame;

import ca.bcit.comp2522.project.menu.JavaFXGame;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.Arrays;

/**
 * A concrete implementation of a Number Game that displays a graphical user interface (GUI) using JavaFX.
 * <p>
 * This class implements the {@link JavaFXGame} interface and extends {@link Application} to provide a standalone
 * JavaFX application for the Number Game. The game involves placing randomly generated numbers in a grid
 * while maintaining a strictly ascending order. The class manages the game board, user interactions via grid buttons,
 * and updates to an embedded scoreboard to track wins, losses, and placements.
 * </p>
 * <p>
 * The main features include:
 * <ul>
 *   <li>Setting up a grid-based UI with configurable rows and columns.</li>
 *   <li>Random generation and shuffling of numbers for placement on the board.</li>
 *   <li>Handling user input through button clicks with validation to maintain ascending order.</li>
 *   <li>Displaying game status messages and a confirmation dialog when the game is over.</li>
 *   <li>Delegating scoreboard updates (games played, wins, losses, and total placements) to a {@link BasicScoreboard}
 *   instance.</li>
 * </ul>
 * </p>
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class NumberGame extends Application
        implements JavaFXGame, javafx.event.EventHandler<javafx.event.ActionEvent>
{
    private CountDownLatch gameLatch;
    private static final int ROWS = 4;
    private static final int COLS = 5;
    private static final int TOTAL_NUMBERS_TO_PLACE = 20;
    private static final int NUMBER_UPPERBOUND = 1000;
    private static final int ZERO_VALUE = 0;
    private static final int SCENE_WIDTH_IN_PIXELS = 400;
    private static final int SCENE_HEIGTH_IN_PIXELS = 260;
    private static final int HBOX_HEIGHT_IN_PIXELS = 20;
    private static final int VBOX_WIDTH_IN_PIXELS = 10;
    private static final int BUTTON_MIN_WIDTH_IN_PIXELS = 60;
    private static final int BUTTON_MIN_HEIGHT_IN_PIXELS = 40;
    private static final int INTEGER_RANGE_ZERO = 0;
    private static final int INTEGER_RANGE_ONE = 1;
    private static final int INTEGER_VALUE_ZERO = 0;
    private static final int GRID_PANE_GAP_IN_PIXELS = 5;
    private static final double A_HUNDRED_PERCENT = 100.0;
    private static final int ROW_INDEX_DEFAULT_VALUE = -1;
    private static final int COLUMN_INDEX_DEFAULT_VALUE = -1;
    private static final int RANGE_ADJUSTER_ONE = 1;
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
    private List<Integer> availableNumbers;
    private int nextNumberIndex;

    /**
     * Sets up and displays the JavaFX UI for the Number Game.
     * <p>
     * This method initializes the main stage and scene with a grid-based layout. It sets up the following:
     * <ul>
     *   <li>A {@link GridPane} that arranges buttons corresponding to grid cells, where each button can receive user
     *   input.</li>
     *   <li>Labels to display the "next number" prompt and the current game status.</li>
     *   <li>A layout hierarchy composed of a {@link VBox} and {@link HBox} for organizing the top and bottom UI
     *   elements.</li>
     *   <li>A stylesheet from "/styles.css" to define the UI's appearance.</li>
     *   <li>An event handler for window close requests that triggers a custom shutdown routine.</li>
     * </ul>
     * Finally, the method calls {@link #resetGame()} to initialize a new game.
     * </p>
     *
     * @param primaryStage the primary stage provided by the JavaFX framework.
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
        gridPane.setHgap(GRID_PANE_GAP_IN_PIXELS);
        gridPane.setVgap(GRID_PANE_GAP_IN_PIXELS);
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.setMaxHeight(Double.MAX_VALUE);

        IntStream.range(INTEGER_RANGE_ZERO, COLS).forEach(i ->
        {
            final ColumnConstraints cc;
            cc = new ColumnConstraints();
            cc.setPercentWidth(A_HUNDRED_PERCENT / COLS);
            gridPane.getColumnConstraints().add(cc);
        });

        IntStream.range(INTEGER_RANGE_ZERO, ROWS).forEach(i ->
        {
            final RowConstraints rc;
            rc = new RowConstraints();
            rc.setPercentHeight(A_HUNDRED_PERCENT / ROWS);
            gridPane.getRowConstraints().add(rc);
        });

        IntStream.range(INTEGER_RANGE_ZERO, ROWS).forEach(r ->
                IntStream.range(INTEGER_RANGE_ZERO, COLS).forEach(c ->
                {
                    Button b = new Button("");
                    b.setMinWidth(BUTTON_MIN_WIDTH_IN_PIXELS);
                    b.setMinHeight(BUTTON_MIN_HEIGHT_IN_PIXELS);
                    b.setOnAction(this);
                    gridPane.add(b, c, r);
                    buttons[r][c] = b;
                })
        );

        nextNumberLabel = new Label("Next number: ");
        statusLabel = new Label("Status: Playing...");

        final VBox topBar;
        topBar = new VBox(VBOX_WIDTH_IN_PIXELS, nextNumberLabel, statusLabel);
        final HBox bottomBar;
        bottomBar = new HBox(HBOX_HEIGHT_IN_PIXELS);

        final VBox root;
        root = new VBox(VBOX_WIDTH_IN_PIXELS, topBar, gridPane, bottomBar);
        root.setFillWidth(true);

        final Scene scene;
        scene = new Scene(root, SCENE_WIDTH_IN_PIXELS, SCENE_HEIGTH_IN_PIXELS);
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
     * Starts the Number Game on a new Stage.
     * <p>
     * This method is invoked when the game is launched via the {@link JavaFXGame} interface (for example,
     * from a main menu). It assigns the provided {@link CountDownLatch} to enable external synchronization,
     * creates a new stage, and calls {@link #start(Stage)} to initialize the game UI.
     * </p>
     *
     * @param latch a {@link CountDownLatch} that will be decremented when the game window closes,
     *              allowing any waiting external thread (such as a main menu) to resume.
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
     * Resets the game state to start a new game.
     * <p>
     * This method performs the following steps:
     * <ul>
     *   <li>Clears the board data by filling the board array with zeroes.</li>
     *   <li>Clears all button labels in the grid.</li>
     *   <li>Resets the counters for numbers placed and game-over flag.</li>
     *   <li>Initializes the list of available numbers and shuffles them.</li>
     *   <li>Generates the first random number to be placed and updates the corresponding UI label.</li>
     *   <li>Increments the games played count in the scoreboard.</li>
     * </ul>
     * </p>
     */
    public void resetGame()
    {
        // Clear the board array
        Arrays.fill(board, ZERO_VALUE);

        // Clear all button labels
        Arrays.stream(buttons)
                .forEach(row -> Arrays.stream(row)
                        .forEach(button -> button.setText("")));

        numbersPlaced = ZERO_VALUE;
        gameOver = false;

        statusLabel.setText("Status: Playing...");

        // Initialize the list of available numbers
        availableNumbers = new ArrayList<>();
        for(int i = 1; i <= NUMBER_UPPERBOUND; i++)
        {
            availableNumbers.add(i);
        }
        Collections.shuffle(availableNumbers);
        nextNumberIndex = INTEGER_VALUE_ZERO;

        // Start a new random number
        nextNumber = getRandomNumber();
        updateNextNumberLabel();

        // We have not started a new official game until now, so increment
        incrementGamesPlayed();
    }

    /**
     * Main event handler for grid button clicks.
     * <p>
     * This method is triggered whenever a grid button is clicked. It processes the move by:
     * <ul>
     *   <li>Determining the row and column of the clicked button.</li>
     *   <li>Verifying that the corresponding board cell is empty.</li>
     *   <li>Permanently placing the next random number if the move is valid.</li>
     *   <li>Checking if the board numbers are in strictly ascending order; if not, the move is reverted and a loss is
     *   signaled.</li>
     *   <li>Updating the UI and game status accordingly, including checking win or loss conditions.</li>
     * </ul>
     * If the board is completely filled or a move violates the ascending order rule, the game is ended and the
     * scoreboard is updated.
     * </p>
     *
     * @param event the {@link ActionEvent} triggered by clicking on a grid button.
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
        int rowIndex;
        int colIndex;

        rowIndex = ROW_INDEX_DEFAULT_VALUE;
        colIndex = COLUMN_INDEX_DEFAULT_VALUE;

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

        final int boardIndex;
        boardIndex = rowIndex * COLS + colIndex;
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
     * Generates and returns a random number from the available numbers list.
     * <p>
     * This method returns the next number from the shuffled list of available numbers.
     * If all numbers have been used, it returns zero.
     * </p>
     *
     * @return an integer representing the next random number, or zero if no numbers are left.
     */
    private int getRandomNumber()
    {
        if(nextNumberIndex < availableNumbers.size())
        {
            return availableNumbers.get(nextNumberIndex++);
        }
        else
        {
            return ZERO_VALUE;
        }
    }

    /*
     * Updates the nextNumberLabel to display the nextNumber.
     */
    private void updateNextNumberLabel()
    {
        nextNumberLabel.setText("Next number: " + nextNumber);
    }

    /*
     * Determines if the board's non-zero entries are in strictly ascending order.
     * <p>
     * This method filters out zero values (empty cells) and then verifies that
     * the remaining entries are in strictly increasing order.
     * </p>
     *
     * @param arr the board array to check.
     * @return {@code true} if all non-zero numbers are in strictly ascending order; {@code false} otherwise.
     */
    private boolean isBoardAscending(final int[] arr)
    {
        int[] nonZero = Arrays.stream(arr)
                .filter(v -> v != INTEGER_VALUE_ZERO)
                .toArray();
        return IntStream.range(INTEGER_RANGE_ONE, nonZero.length)
                .allMatch(i -> nonZero[i] > nonZero[i - RANGE_ADJUSTER_ONE]);
    }

    /*
     * Checks if there is at least one valid cell on the board where the next number can be placed.
     * <p>
     * This method attempts to hypothetically place the candidate number in every empty cell.
     * If placing the number in any empty cell results in a strictly ascending board, then there is a valid move.
     * </p>
     *
     * @param n   the candidate number to test.
     * @param arr the board array.
     * @return {@code true} if at least one valid placement exists; {@code false} otherwise.
     */
    private boolean canPlaceNextNumber(final int n,
                                       final int[] arr)
    {
        return IntStream.range(INTEGER_RANGE_ZERO, arr.length).anyMatch(i ->
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
     * Displays a game-over dialog prompting the user to try again or quit.
     * <p>
     * The method shows a confirmation dialog with a message derived from the current status.
     * If the user selects "Try Again," the game is reset by calling {@link #resetGame()}. Otherwise, the primary stage
     * is closed.
     * </p>
     *
     * @param stage the primary {@link Stage} on which to display the dialog.
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