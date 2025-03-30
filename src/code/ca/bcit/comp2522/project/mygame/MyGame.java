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
import static ca.bcit.comp2522.project.mygame.common.GameConfig.TOP_MARGIN_IN_PIXEL;
import static ca.bcit.comp2522.project.mygame.util.DrawingUtils.directionDeltaX;
import static ca.bcit.comp2522.project.mygame.util.DrawingUtils.directionDeltaY;

public class MyGame extends Application implements JavaFXGame {
    private Stage myGameStage;
    private AnimationTimer gameLoop;  // store a reference
    private GameLogic gameLogic;
    private SoundManager soundManager;

    // Game grid constants
    private static final int CANVAS_WIDTH = GRID_WIDTH_IN_NUMBER_OF_CELLS * CELL_SIZE_IN_PIXEL;
    private static final int CANVAS_HEIGHT = GRID_HEIGHT_IN_NUMBER_OF_CELLS * CELL_SIZE_IN_PIXEL + TOP_MARGIN_IN_PIXEL + BOTTOM_MARGIN_IN_PIXEL;

    // Game states
    private GameState gameState = GameState.INTRO;
    private CountDownLatch gameLatch;

    private Player user;
    private final Random random = new Random();

    // Additional fields for finishing logic.

    // Timing variables (in nanoseconds)
    private long lastUpdateTime = 0;
    // We aim to update game logic roughly every 50ms
    private static final long UPDATE_INTERVAL = 50_000_000;


    @Override
    public void start(Stage primaryStage) {
        this.myGameStage = primaryStage; // Store the stage for future reference.
        soundManager = new SoundManager();
        gameLogic = new GameLogic(soundManager);
        final GameRenderer renderer;
        renderer = new GameRenderer(gameLogic);

        // If the user clicks the [X] button to close the window:
        primaryStage.setOnCloseRequest(e -> {
            // We handle it ourselves so that we can do the latch
            e.consume();            // prevent default close
            closeGameWindow();      // custom method we define next
        });

        // Set up the canvas and scene.
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/myGameStyles.css").toExternalForm());

        primaryStage.setTitle("Red Light Green Light");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Set up key input for all game states.
        scene.setOnKeyPressed(e -> {
            if (gameState == GameState.INTRO) {
                if (e.getCode() == KeyCode.ENTER) {
                    gameLogic.initGame();
                    gameState = GameState.GAME;
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    closeGameWindow();
                }
            } else if (gameState == GameState.GAME) {
                if (!gameLogic.isGameOver()) {
                    MovementDirection dir = null;
                    if (e.getCode() == KeyCode.UP) {
                        dir = MovementDirection.UP;
                    } else if (e.getCode() == KeyCode.DOWN) {
                        dir = MovementDirection.DOWN;
                    } else if (e.getCode() == KeyCode.LEFT) {
                        dir = MovementDirection.LEFT;
                    } else if (e.getCode() == KeyCode.RIGHT) {
                        dir = MovementDirection.RIGHT;
                    }
                    if (dir != null) {
                        gameLogic.tryMoveWithPush(gameLogic.getUser(), directionDeltaX(dir), directionDeltaY(dir), new ArrayList<>(), true);
                    }
                }
            } else if (gameState == GameState.GAME_OVER) {
                if (e.getCode() == KeyCode.ENTER) {
                    gameLogic.initGame();
                    gameState = GameState.GAME;
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    closeGameWindow();
                }
            }
        });

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFont(Font.font("Monospaced", CELL_SIZE_IN_PIXEL));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        // Initialize timing for game logic updates and light switching.
        gameLogic.setNextSwitchInMilliseconds(1500 + random.nextInt(4000)); // green light duration
        gameLogic.setLastLightSwitchTimeInNanoseconds(System.nanoTime());

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }
                long delta = now - lastUpdateTime;
                if (delta >= UPDATE_INTERVAL) {
                    // If you still need to update game logic, call updateGame(now) here.
                    if (gameState == GameState.GAME) {
                        gameLogic.updateGame(now);
                    }

                    if (gameLogic.isGameOver()) {
                        gameState = GameState.GAME_OVER;
                    }

                    renderer.render(gameState, gc);

                    lastUpdateTime = now;
                }

                if (gameState == GameState.INTRO) {
                    soundManager.playBGM();
                } else if (gameState == GameState.GAME) {
                    if (gameLogic.isGreen()) {
                        soundManager.playBGM();
                    } else {
                        soundManager.stopBGM();
                    }
                } else { // GAME_OVER or other states
                    soundManager.stopBGM();
                }
            }
        };
        gameLoop.start();
    }

    @Override
    public void play(final CountDownLatch latch) {
        this.gameLatch = latch;
        Stage stage = new Stage();
        start(stage);
    }

    /*
     * Call this when we want to close the game window and let the main menu continue.
     */
    private void closeGameWindow() {
        soundManager.stopBGM();

        // Stop the game loop.
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (gameLatch != null) {
            gameLatch.countDown();  // unblocks main-menu thread
        }
        if (myGameStage != null) {
            myGameStage.close();
        }
    }

    @Override
    public void stop() {
        // This method is called when the application is exiting.
        // Do any final cleanup here.
        if (gameLoop != null) {
            gameLoop.stop();
        }
        soundManager.stopBGM();
    }
}