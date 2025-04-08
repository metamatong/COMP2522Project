package ca.bcit.comp2522.project.mygame;

import ca.bcit.comp2522.project.menu.JavaFXGame;
import ca.bcit.comp2522.project.mygame.audio.SoundManager;
import ca.bcit.comp2522.project.mygame.common.GameState;
import ca.bcit.comp2522.project.mygame.common.MovementDirection;
import ca.bcit.comp2522.project.mygame.engine.GameLogic;
import ca.bcit.comp2522.project.mygame.entities.Player;
import ca.bcit.comp2522.project.mygame.ui.GameRenderer;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static ca.bcit.comp2522.project.mygame.common.GameConfig.BOTTOM_MARGIN_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.CELL_SIZE_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.GRID_HEIGHT_IN_NUMBER_OF_CELLS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.GRID_WIDTH_IN_NUMBER_OF_CELLS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.LIGHT_SWITCH_FROM_RED_LIGHT_MINIMUM_INTERVAL_IN_MILLISECONDS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.LIGHT_SWITCH_MINIMUM_INTERVAL_IN_MILLISECONDS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.TOP_MARGIN_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.util.DrawingUtils.directionDeltaX;
import static ca.bcit.comp2522.project.mygame.util.DrawingUtils.directionDeltaY;


/**
 * The {@code MyGame} class is the primary entry point for the "Red Light Green Light" game.
 * <p>
 * This class extends {@link Application} and implements the {@link JavaFXGame} interface,
 * combining the JavaFX application lifecycle with custom game logic. It is responsible for initializing
 * the game window, configuring the scene and canvas, processing user input (keyboard events), and managing
 * the game loop via an {@link AnimationTimer}. The class orchestrates transitions between different game
 * states (such as the intro screen, active gameplay, and game over screen) and integrates with components
 * that handle game logic, sound, and UI rendering.
 * </p>
 * <p>
 * Key features include:
 * <ul>
 *   <li>Setting up the stage and canvas for drawing game graphics.</li>
 *   <li>Managing user input to trigger game state changes and in-game movements.</li>
 *   <li>Running a game loop that periodically updates game logic and renders the UI.</li>
 *   <li>Controlling background music based on game conditions.</li>
 * </ul>
 * </p>
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class MyGame
        extends Application
        implements JavaFXGame
{
    private static final int CANVAS_WIDTH = GRID_WIDTH_IN_NUMBER_OF_CELLS * CELL_SIZE_IN_PIXEL;
    private static final int CANVAS_HEIGHT = GRID_HEIGHT_IN_NUMBER_OF_CELLS * CELL_SIZE_IN_PIXEL +
                                             TOP_MARGIN_IN_PIXEL +
                                             BOTTOM_MARGIN_IN_PIXEL;
    private static final int DEFAULT_TIME_VALUE = 0;

    // Game logic update roughly every 50ms
    private static final long UPDATE_INTERVAL_IN_NANOSECONDS = 50_000_000;

    private Stage myGameStage;
    private AnimationTimer gameLoop;  // store a reference
    private GameLogic gameLogic;
    private SoundManager soundManager;

    // Game states
    private GameState gameState = GameState.INTRO;
    private CountDownLatch gameLatch;

    private Player user;
    private final Random random = new Random();

    private long lastUpdateTimeInNanoseconds = 0;

    /**
     * Initializes and starts the game.
     * <p>
     * This method is called by the JavaFX framework when the application launches.
     * It performs the following tasks:
     * <ul>
     *   <li>Stores the primary stage and sets up a custom close request handler.</li>
     *   <li>Initializes the {@link SoundManager} and {@link GameLogic}, and creates an instance
     *       of {@link GameRenderer} for handling UI drawing.</li>
     *   <li>Creates the canvas and scene, applying any required stylesheets.</li>
     *   <li>Registers key event handlers to handle user input across different game states:
     *       starting the game, moving the user-controlled player, or exiting the game.</li>
     *   <li>Configures initial timing values for game logic updates and light switching.</li>
     *   <li>Creates and starts the main game loop using {@link AnimationTimer} to continuously
     *       update game logic and render the current game state.</li>
     * </ul>
     * </p>
     *
     * @param primaryStage the primary {@link Stage} for this application.
     */
    @Override
    public void start(final Stage primaryStage)
    {
        this.myGameStage = primaryStage; // Store the stage for future reference.
        soundManager = new SoundManager();
        gameLogic = GameLogic.getInstance(soundManager);
        final GameRenderer renderer;
        renderer = new GameRenderer(gameLogic);

        // If the user clicks the [X] button to close the window:
        primaryStage.setOnCloseRequest(e ->
        {
            // We handle it ourselves so that we can do the latch
            e.consume(); // prevent default close
            closeGameWindow(); // custom method we define next
        });

        // Set up the canvas and scene.
        final Canvas canvas;
        final StackPane root;
        final Scene scene;

        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        root = new StackPane(canvas);
        scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/myGameStyles.css").toExternalForm());

        primaryStage.setTitle("Red Light BLOOD Light");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Set up key input for all game states.
        scene.setOnKeyPressed(e ->
        {
            if(gameState == GameState.INTRO)
            {
                if(e.getCode() == KeyCode.ENTER)
                {
                    gameLogic.initGame();
                    gameState = GameState.GAME;
                }
                else if
                (e.getCode() == KeyCode.ESCAPE)
                {
                    closeGameWindow();
                }
            }
            else if(gameState == GameState.GAME)
            {
                if(!gameLogic.isGameOver())
                {
                    MovementDirection dir;
                    dir = null;
                    if(e.getCode() == KeyCode.UP)
                    {
                        dir = MovementDirection.UP;
                    }
                    else if(e.getCode() == KeyCode.DOWN)
                    {
                        dir = MovementDirection.DOWN;
                    }
                    else if(e.getCode() == KeyCode.LEFT)
                    {
                        dir = MovementDirection.LEFT;
                    }
                    else if(e.getCode() == KeyCode.RIGHT)
                    {
                        dir = MovementDirection.RIGHT;
                    }
                    if(dir != null)
                    {
                        gameLogic.tryMoveWithPush(gameLogic.getUser(), directionDeltaX(dir), directionDeltaY(dir), new ArrayList<>(), true);
                    }
                }
            }
            else if(gameState == GameState.GAME_OVER)
            {
                if(e.getCode() == KeyCode.ENTER)
                {
                    gameLogic.initGame();
                    gameState = GameState.GAME;
                }
                else if(e.getCode() == KeyCode.ESCAPE)
                {
                    closeGameWindow();
                }
            }
        });

        final GraphicsContext gc;
        gc = canvas.getGraphicsContext2D();
        gc.setFont(Font.font("Monospaced", CELL_SIZE_IN_PIXEL));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        // Initialize timing for game logic updates and light switching.
        gameLogic.setNextSwitchInMilliseconds(LIGHT_SWITCH_MINIMUM_INTERVAL_IN_MILLISECONDS +
                random.nextInt(LIGHT_SWITCH_FROM_RED_LIGHT_MINIMUM_INTERVAL_IN_MILLISECONDS));
        gameLogic.setLastLightSwitchTimeInNanoseconds(System.nanoTime());

        gameLoop = new AnimationTimer()
        {
            @Override
            public void handle(final long now)
            {
                if(lastUpdateTimeInNanoseconds == DEFAULT_TIME_VALUE)
                {
                    lastUpdateTimeInNanoseconds = now;
                    return;
                }
                final long delta;
                delta = now - lastUpdateTimeInNanoseconds;
                if(delta >= UPDATE_INTERVAL_IN_NANOSECONDS)
                {
                    if(gameState == GameState.GAME)
                    {
                        gameLogic.updateGame(now);
                    }

                    if(gameLogic.isGameOver())
                    {
                        gameState = GameState.GAME_OVER;
                    }

                    renderer.render(gameState, gc);

                    lastUpdateTimeInNanoseconds = now;
                }

                if(gameState == GameState.INTRO)
                {
                    soundManager.playBGM();
                }
                else if(gameState == GameState.GAME)
                {
                    if(gameLogic.isGreen())
                    {
                        soundManager.playBGM();
                    }
                    else
                    {
                        soundManager.stopBGM();
                    }
                }
                else
                {
                    soundManager.stopBGM();
                }
            }
        };
        gameLoop.start();
    }

    /**
     * Starts the game on a new stage.
     * <p>
     * This method is part of the {@link JavaFXGame} interface, enabling the game to be launched
     * from an external context (such as a main menu) by creating a new stage. When the game window
     * is closed, a {@link CountDownLatch} (if provided) will be decremented to unblock any waiting threads.
     * </p>
     *
     * @param latch a {@link CountDownLatch} that will be decremented when the game window closes,
     *              allowing external processes (e.g., main menu) to resume execution.
     */
    @Override
    public void play(final CountDownLatch latch)
    {
        this.gameLatch = latch;
        final Stage stage;
        stage = new Stage();
        start(stage);
    }

    /**
     * Stops the game.
     * <p>
     * This method is automatically invoked when the application is stopped. It stops the active game loop
     * and halts any background music, ensuring that all resources are properly released and the game state is
     * gracefully terminated.
     * </p>
     */
    @Override
    public void stop()
    {
        if(gameLoop != null)
        {
            gameLoop.stop();
        }
        soundManager.stopBGM();
    }

    /*
     * Closes the game window.
     * <p>
     * This helper method is invoked when a user opts to exit the game (or when the window's close request is
     * triggered). It performs the following actions:
     * <ul>
     *   <li>Stops background music and the game loop.</li>
     *   <li>Decrements the {@link CountDownLatch} if one is present, unblocking any waiting threads (such as a main
     *   menu).</li>
     *   <li>Closes the primary game stage, effectively terminating the UI.</li>
     * </ul>
     * </p>
     */
    private void closeGameWindow()
    {
        soundManager.stopBGM();

        if(gameLoop != null)
        {
            gameLoop.stop();
        }

        if(gameLatch != null)
        {
            gameLatch.countDown();  // unblocks main-menu thread
        }

        if(myGameStage != null)
        {
            myGameStage.close();
        }
    }
}
