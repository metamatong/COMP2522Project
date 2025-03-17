package ca.bcit.comp2522.project;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Random;

/**
 * Concrete NumberGame that uses a BasicScoreboard for tracking stats and runs a JavaFX GUI.
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class NumberGame extends Application
        implements javafx.event.EventHandler<javafx.event.ActionEvent> {

    private static final int ROWS = 4;
    private static final int COLS = 5;
    private static final int TOTAL_NUMBERS_TO_PLACE = 20;
    private static final int NUMBER_UPPERBOUND = 1000;
    private static final int ZERO_VALUE = 0;
    private static final int SCENE_WIDTH = 400;
    private static final int SCENE_HEIGTH = 300;
    private static final int HBOX_HEIGHT = 20;
    private static final int VBOX_WIDTH = 10;
    private static final int BUTTON_MIN_WIDTH = 60;


    // Embedded scoreboard
    private BasicScoreboard scoreboard;

    // UI components
    private final Button[][] buttons = new Button[ROWS][COLS];
    private Label nextNumberLabel;
    private Label statusLabel; // shows "You win!" or "You lose!"
    private Button tryAgainButton;
    private Button quitButton;

    // Board data: 20 slots, 0 means "empty"
    private final int[] board = new int[ROWS * COLS];

    // Current game state
    private final Random random = new Random();
    private int nextNumber;      // The next random number to place
    private int numbersPlaced;   // How many we've successfully placed so far
    private boolean gameOver;    // true if player has won or lost already

    @Override
    public void start(final Stage primaryStage)
    {
        scoreboard = new BasicScoreboard();

        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.setMaxHeight(Double.MAX_VALUE);

        for(int i = 0; i < COLS; i++)
        {
            final ColumnConstraints cc;
            cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / COLS);
            gridPane.getColumnConstraints().add(cc);
        }

        for(int i = 0; i < ROWS; i++)
        {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / ROWS);
            gridPane.getRowConstraints().add(rc);
        }

        for(int r = 0; r < ROWS; r++)
        {
            for(int c = 0; c < COLS; c++)
            {
                final Button b;
                b = new Button("");
                b.setMinWidth(BUTTON_MIN_WIDTH);
                b.setOnAction(this);  // all buttons use the same event handler
                gridPane.add(b, c, r);
                buttons[r][c] = b;
            }
        }

        nextNumberLabel = new Label("Next number: ");
        statusLabel = new Label("Status: Playing...");

        tryAgainButton = new Button("Try Again");
        tryAgainButton.setOnAction(e -> {
            resetGame();
        });

        quitButton = new Button("Quit");
        quitButton.setOnAction(e -> {
            // Show final scoreboard, then close the stage (return to Main menu in a bigger app)
            final Alert alert;
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Final Scoreboard");
            alert.setHeaderText("Your final results:");
            alert.setContentText(getScoreboardSummary());
            alert.showAndWait();

            // Close this stage/window.
            primaryStage.close();
        });

        final VBox topBar;
        topBar = new VBox(VBOX_WIDTH, nextNumberLabel, statusLabel);
        final HBox bottomBar;
        bottomBar = new HBox(HBOX_HEIGHT, tryAgainButton, quitButton);

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

    // ---------------------
    // Scoreboard Delegation
    // ---------------------

    public void incrementGamesPlayed()
    {
        scoreboard.incrementGamesPlayed();
    }

    public void incrementWins()
    {
        scoreboard.incrementWins();
    }

    public void incrementLosses()
    {
        scoreboard.incrementLosses();
    }

    public void addToTotalPlacements(final int placementsThisGame)
    {
        scoreboard.addToTotalPlacements(placementsThisGame);
    }

    public String getScoreboardSummary()
    {
        return scoreboard.getScoreboardSummary();
    }

    // -------------
    // Game Methods
    // -------------

    public void resetGame() {
        // Clear the board array
        for(int i = 0; i < board.length; i++)
        {
            board[i] = ZERO_VALUE;
        }
        // Clear all button labels
        for(int r = 0; r < ROWS; r++)
        {
            for(int c = 0; c < COLS; c++)
            {
                buttons[r][c].setText("");
            }
        }
        numbersPlaced = ZERO_VALUE;
        gameOver = false;

        statusLabel.setText("Status: Playing...");

        // Start a new random number
        nextNumber = getRandomNumber();
        updateNextNumberLabel();

        // We have not started a new official game until now, so increment
        incrementGamesPlayed();
    }

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
            // This breaks ascending order => lose
            gameOver = true;
            board[boardIndex] = ZERO_VALUE; // revert
            incrementLosses();
            addToTotalPlacements(numbersPlaced);
            statusLabel.setText("Status: You lose! No place for " + nextNumber);
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
            return;
        }

        // If we can continue, show the next number
        updateNextNumberLabel();
    }

    // ----------------
    // Helper Utilities
    // ----------------

    private int getRandomNumber()
    {
        return random.nextInt(NUMBER_UPPERBOUND) + 1;
    }

    private void updateNextNumberLabel()
    {
        nextNumberLabel.setText("Next number: " + nextNumber);
    }

    private boolean isBoardAscending(final int[] arr)
    {
        int last = -1; // track last non-zero
        for(int value : arr)
        {
            if(value != 0)
            {
                if(value <= last)
                {
                    return false;
                }
                last = value;
            }
        }
        return true;
    }

    private boolean canPlaceNextNumber(final int n, final int[] arr)
    {
        // Try placing 'n' in each empty spot; revert after test
        for(int i = 0; i < arr.length; i++)
        {
            if(arr[i] == ZERO_VALUE)
            {
                arr[i] = n;
                final boolean ascending;
                ascending = isBoardAscending(arr);
                arr[i] = ZERO_VALUE;
                if(ascending)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(final String[] args)
    {
        launch(args);
    }
}