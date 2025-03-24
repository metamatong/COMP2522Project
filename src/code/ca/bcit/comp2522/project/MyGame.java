package ca.bcit.comp2522.project;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyGame extends Application {

    // Game grid constants
    private static final int GRID_WIDTH = 60;
    private static final int GRID_HEIGHT = 40;
    private static final int CELL_SIZE = 20; // pixel size per grid cell
    private static final int CANVAS_WIDTH = GRID_WIDTH * CELL_SIZE;
    private static final int CANVAS_HEIGHT = GRID_HEIGHT * CELL_SIZE;
    private static final int FINISH_LINE_Y = 5; // Change from 0 to 5 (or another value) so players finish sooner.
    private static final int STAT_HEIGHT = CELL_SIZE; // Height for the stats display (you can adjust as needed)

    // Duration to show a dying player (in nanoseconds)
    private static final long DEATH_ANIMATION_DURATION = 300_000_000L; // 300ms

    // Game states
    private enum GameState { INTRO, GAME, GAME_OVER }
    private GameState gameState = GameState.INTRO;
    private long gameStartTime;

    // Player class now gets an extra "finished" flag.
    private static class Player {
        int x, y;
        int prevX, prevY;
        boolean isUser;
        boolean isEliminated;
        boolean finished;  // new: has the player finished?
        // Time (nanoTime) at which the player died.
        long deathTimestamp = 0;

        Player(int x, int y) {
            this.x = x;
            this.y = y;
            this.prevX = x;
            this.prevY = y;
            this.isUser = false;
            this.isEliminated = false;
            this.finished = false;
        }
    }

    private enum Direction { UP, DOWN, LEFT, RIGHT }

    private List<Player> players;
    private Player user;
    private boolean isGreen = true;
    private long nextSwitch; // in ms
    private boolean gameOver = false;
    private final Random random = new Random();
    private boolean bgmPlaying = false;

    // Additional fields for finishing logic.
    private int finishedCount = 0;
    private boolean fieldCleared = false; // once set, remaining players are eliminated

    // Timing variables (in nanoseconds)
    private long lastUpdateTime = 0;
    private long lastLightSwitchTime = 0;
    // We aim to update game logic roughly every 50ms
    private static final long UPDATE_INTERVAL = 50_000_000;

    // For intro screen logo (optional)
    private String[] logoLines;

    // Sound effects
    private AudioClip pushSound;
    private AudioClip gunshotSound;
    private AudioClip deathSound1;
    private AudioClip deathSound2;
    private AudioClip bgm;

    @Override
    public void start(Stage primaryStage) {
        // Set up the canvas and scene.
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/myGameStyles.css").toExternalForm());

        primaryStage.setTitle("Red Light Green Light");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load logo from resources (if available)
        try {
            logoLines = loadLogo();
        } catch (IOException e) {
            logoLines = new String[]{"RED LIGHT", "GREEN LIGHT"};
        }

        // Load sound effects.
        pushSound = new AudioClip(getClass().getResource("/push.wav").toExternalForm());
        gunshotSound = new AudioClip(getClass().getResource("/gunshot.mp3").toExternalForm());
        deathSound1 = new AudioClip(getClass().getResource("/deathone.mp3").toExternalForm());
        deathSound2 = new AudioClip(getClass().getResource("/deathtwo.mp3").toExternalForm());
        bgm = new AudioClip(getClass().getResource("/bgm.mp3").toExternalForm()); //https://soundcloud.com/extiox/squid-game-red-light-green-light

        // Set up key input for all game states.
        scene.setOnKeyPressed(e -> {
            if (gameState == GameState.INTRO) {
                if (e.getCode() == KeyCode.ENTER) {
                    initGame();
                    gameState = GameState.GAME;
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    Platform.exit();
                }
            } else if (gameState == GameState.GAME) {
                if (!gameOver) {
                    Direction dir = null;
                    if (e.getCode() == KeyCode.UP) {
                        dir = Direction.UP;
                    } else if (e.getCode() == KeyCode.DOWN) {
                        dir = Direction.DOWN;
                    } else if (e.getCode() == KeyCode.LEFT) {
                        dir = Direction.LEFT;
                    } else if (e.getCode() == KeyCode.RIGHT) {
                        dir = Direction.RIGHT;
                    }
                    if (dir != null) {
                        tryMoveWithPush(user, directionDeltaX(dir), directionDeltaY(dir), new ArrayList<>(), true);
                    }
                }
            } else if (gameState == GameState.GAME_OVER) {
                if (e.getCode() == KeyCode.ENTER) {
                    initGame();
                    gameState = GameState.GAME;
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    Platform.exit();
                }
            }
        });

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFont(Font.font("Monospaced", CELL_SIZE));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        // Initialize timing for game logic updates and light switching.
        nextSwitch = 1500 + random.nextInt(2000); // green light duration
        lastLightSwitchTime = System.nanoTime();

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }
                long delta = now - lastUpdateTime;
                if (delta >= UPDATE_INTERVAL) {
                    switch (gameState) {
                        case INTRO:
                            drawIntroScreen(gc);
                            break;
                        case GAME:
                            updateGame(now);
                            drawGame(gc);
                            break;
                        case GAME_OVER:
                            drawGameOverScreen(gc);
                            break;
                    }
                    lastUpdateTime = now;
                }

                // Update background music state based on gameState.
                if (gameState == GameState.INTRO && !bgmPlaying) {
                    bgm.setCycleCount(AudioClip.INDEFINITE); // loop forever
                    bgm.play();
                    bgmPlaying = true;
                } else if (gameState != GameState.INTRO && bgmPlaying) {
                    bgm.stop();
                    bgmPlaying = false;
                }
            }
        };
        gameLoop.start();
    }

    // Helper: convert a Direction into an x delta.
    private int directionDeltaX(Direction d) {
        switch (d) {
            case LEFT: return -1;
            case RIGHT: return 1;
            default: return 0;
        }
    }

    // Helper: convert a Direction into a y delta.
    private int directionDeltaY(Direction d) {
        switch (d) {
            case UP: return -1;
            case DOWN: return 1;
            default: return 0;
        }
    }

    // Loads the ASCII art logo.
    private String[] loadLogo() throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/logo.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            if (is == null) throw new IOException("Logo resource not found!");
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines.toArray(new String[0]);
    }

    // Initializes the game state.
    private void initGame() {
        players = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            int startX = random.nextInt(GRID_WIDTH);
            Player p = new Player(startX, GRID_HEIGHT - 1);
            if (i == 0) {
                p.isUser = true;
                user = p;
            }
            players.add(p);
        }

        gameStartTime = System.nanoTime();
        // Reset finishing fields.
        finishedCount = 0;
        fieldCleared = false;
        isGreen = true;
        gameOver = false;
        nextSwitch = 1500 + random.nextInt(2000);
        lastLightSwitchTime = System.nanoTime();
    }

    // Updates game logic.
    private void updateGame(long now) {
        // Process NPC voluntary moves.
        if (isGreen) {
            for (Player p : players) {
                if (!p.isEliminated && !p.isUser && random.nextDouble() < 0.1) {
                    tryMoveWithPush(p, 0, -1, new ArrayList<>(), true);
                }
            }
        } else {
            // During red light, occasional moves.
            for (Player p : players) {
                if (!p.isEliminated && !p.isUser && random.nextDouble() < 0.001) {
                    Direction d = Direction.values()[random.nextInt(4)];
                    tryMoveWithPush(p, directionDeltaX(d), directionDeltaY(d), new ArrayList<>(), true);
                }
            }
        }

        // --- FINISH LINE LOGIC ---
        // Process finishers: any player at y==0 (finish line) and not yet finished.
        for (Player p : players) {
            if (!p.isEliminated && !p.finished && p.y < FINISH_LINE_Y) {
                if (finishedCount < 10) {
                    p.finished = true;
                    finishedCount++;
                } else {
                    // Finished too late: eliminate this player.
                    p.isEliminated = true;
                    p.deathTimestamp = now;
                    playDeathSequence();
                }
            }
        }
        // If 10 players have finished, then eliminate everyone else.
// If 10 players have finished, then eliminate everyone else.
        if (finishedCount >= 10 && !fieldCleared) {
            int index = 0;
            for (Player p : players) {
                if (!p.finished && !p.isEliminated) {
                    p.isEliminated = true;
                    p.deathTimestamp = now;
                    // Schedule each death sequence with an increasing delay.
                    // For example: first player gets 0.5s delay, second 0.6s, third 0.7s, etc.
                    scheduleDeathSequence(p, 0.5 + index * 0.05);
                    index++;
                }
            }
            fieldCleared = true;
            gameOver = true;
        }
        // --- END FINISH LINE LOGIC ---

        // Red light elimination: if a player moves during red light and they are not finished, eliminate them.
        if (!isGreen) {
            for (Player p : players) {
                if (!p.isEliminated && !p.finished && (p.x != p.prevX || p.y != p.prevY)) {
                    p.isEliminated = true;
                    p.deathTimestamp = now;
                    playDeathSequence();
                    if (p.isUser) gameOver = true;
                }
            }
        }

        // Win condition: if the user has finished.
        if (user.y <= FINISH_LINE_Y && !user.isEliminated) {
            // The user finished.
            user.finished = true;
            gameOver = true;
        }

        // Update light timer.
        long elapsedMs = (now - lastLightSwitchTime) / 1_000_000;
        if (elapsedMs >= nextSwitch) {
            isGreen = !isGreen;
            lastLightSwitchTime = now;
            // Longer red light duration.
            if (isGreen) {
                nextSwitch = 1500 + random.nextInt(2000);
            } else {
                nextSwitch = 3000 + random.nextInt(2000);
            }
        }

        // Update previous positions.
        for (Player p : players) {
            p.prevX = p.x;
            p.prevY = p.y;
        }

        if (gameOver) {
            gameState = GameState.GAME_OVER;
        }
    }

    // Plays the death sound sequence: gunshot immediately, then (after 0.5s) a random death sound.
    private void playDeathSequence() {
        gunshotSound.play();
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        delay.setOnFinished(event -> {
            if (random.nextBoolean()) {
                deathSound1.play();
            } else {
                deathSound2.play();
            }
        });
        delay.play();
    }

    /*
     * Attempts to move player p by (dx, dy).
     * For voluntary (initiating) moves, if the destination cell is occupied,
     * p does not change its own cell (thus staying hidden) but attempts to push the occupant.
     * For forced moves (initiating == false), the player always moves.
     * If a push is initiated by the user, a sound effect is played.
     */
    private boolean tryMoveWithPush(Player p, int dx, int dy, List<Player> visited, boolean initiating) {
        if (visited.contains(p)) return false;
        visited.add(p);
        int newX = p.x + dx;
        int newY = p.y + dy;
        if (newX < 0 || newX >= GRID_WIDTH || newY < 0 || newY >= GRID_HEIGHT) return false;
        Player occupant = getPlayerAt(newX, newY);
        if (occupant != null) {
            if (initiating && p.isUser) {
                pushSound.play();
            }
            int oldX = p.x;
            int oldY = p.y;
            boolean pushed = tryMoveWithPush(occupant, dx, dy, visited, false);
            if (!pushed) return false;
            if (initiating) {
                p.x = oldX;
                p.y = oldY;
            } else {
                p.x = newX;
                p.y = newY;
            }
            return true;
        } else {
            p.x = newX;
            p.y = newY;
            return true;
        }
    }

    // Returns the player occupying the given grid cell, or null.
    private Player getPlayerAt(int x, int y) {
        for (Player p : players) {
            if (!p.isEliminated && p.x == x && p.y == y) {
                return p;
            }
        }
        return null;
    }

    // Draws the game field and players.
    private void drawGame(GraphicsContext gc) {
        // Compute elapsed time in seconds.
        double elapsedSeconds = (System.nanoTime() - gameStartTime) / 1_000_000_000.0;

        // Count dead players.
        int deadCount = 0;
        for (Player p : players) {
            if (p.isEliminated) {
                deadCount++;
            }
        }

        // Clear previous stats
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_WIDTH, STAT_HEIGHT);

        // Prepare the stats string.
        String stats = String.format("Time: %.1fs   Finished: %d   Dead: %d",
                elapsedSeconds, finishedCount, deadCount);

        // Draw the stats at the top left (using a smaller font so it fits nicely).
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Monospaced", CELL_SIZE / 2));
        gc.fillText(stats, 5, CELL_SIZE / 2);

        gc.setFill(Color.BLACK);
        // Clear only below the header area.
        gc.fillRect(0, STAT_HEIGHT, CANVAS_WIDTH, CANVAS_HEIGHT - STAT_HEIGHT);

        gc.setFont(Font.font("Monospaced", CELL_SIZE));
        gc.setTextAlign(TextAlignment.LEFT);
        for (int x = 0; x < GRID_WIDTH; x++) {
            gc.setFill(isGreen ? Color.GREEN : Color.RED);
            gc.fillText("*", x * CELL_SIZE, STAT_HEIGHT);
        }

        long now = System.nanoTime();
        for (Player p : players) {
            // Skip finished players so they no longer appear on-screen.
            if (p.finished) continue;
            if (p.isEliminated) {
                drawDeadSprite(gc, p);
            } else {
                drawPlayerSprite(gc, p);
            }
        }
    }

    // Draws a player's ASCII sprite.
    private void drawPlayerSprite(GraphicsContext gc, Player p) {
        double baseX = (p.x - 1) * CELL_SIZE;
        double baseY = p.y * CELL_SIZE + STAT_HEIGHT;  // add the offset here
        if (p.isUser) {
            gc.setFill(Color.CYAN);
            putSafeString(gc, baseX, baseY - 3 * CELL_SIZE, "YOU");
            putSafeString(gc, baseX, baseY - 2 * CELL_SIZE, " O ");
            putSafeString(gc, baseX, baseY - 1 * CELL_SIZE, "/|\\");
            putSafeString(gc, baseX, baseY, "/ \\");
        } else {
            gc.setFill(Color.WHITE);
            putSafeString(gc, baseX, baseY - 2 * CELL_SIZE, " O ");
            putSafeString(gc, baseX, baseY - 1 * CELL_SIZE, "/|\\");
            putSafeString(gc, baseX, baseY, "/ \\");
        }
    }

    // Helper: computes text width for centering.
    private double computeTextWidth(String text, Font font) {
        Text tempText = new Text(text);
        tempText.setFont(font);
        return tempText.getLayoutBounds().getWidth();
    }

    // Draws a string if within canvas bounds.
    private void putSafeString(GraphicsContext gc, double x, double y, String s) {
        if (x + s.length() * CELL_SIZE / 2 < 0 || x > CANVAS_WIDTH) return;
        if (y < 0 || y > CANVAS_HEIGHT) return;
        gc.fillText(s, x, y);
    }

    // Draws the intro screen.
    private void drawIntroScreen(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", CELL_SIZE));

        int logoHeight = logoLines.length;
        double logoY = CANVAS_HEIGHT / 2 - (logoHeight * CELL_SIZE) / 2 - CELL_SIZE;
        for (int i = 0; i < logoLines.length; i++) {
            String line = logoLines[i];
            double textWidth = computeTextWidth(line, gc.getFont());
            double x = (CANVAS_WIDTH - textWidth) / 2;
            gc.fillText(line, x, logoY + i * CELL_SIZE);
        }
        String titlePart1 = "WELCOME TO RED LIGHT ";
        String titlePart2 = " LIGHT";
        String redWord = "BLOOD";
        String fullTitle = titlePart1 + redWord + titlePart2;
        double titleY = logoY + logoHeight * CELL_SIZE + CELL_SIZE;
        double titleWidth = computeTextWidth(fullTitle, gc.getFont());
        double titleX = (CANVAS_WIDTH - titleWidth) / 2;
        gc.setFill(Color.WHITE);
        gc.fillText(titlePart1, titleX, titleY);
        double offset = computeTextWidth(titlePart1, gc.getFont());
        gc.setFill(Color.RED);
        gc.fillText(redWord, titleX + offset, titleY);
        offset += computeTextWidth(redWord, gc.getFont());
        gc.setFill(Color.WHITE);
        gc.fillText(titlePart2, titleX + offset, titleY);

        String instructions = "Press ENTER to PLAY or ESC to EXIT";
        double instrWidth = computeTextWidth(instructions, gc.getFont());
        double instrX = (CANVAS_WIDTH - instrWidth) / 2;
        double instrY = titleY + CELL_SIZE * 2;
        gc.fillText(instructions, instrX, instrY);
    }

    // Draws the game over screen.
    private void drawGameOverScreen(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", CELL_SIZE));

        String message = user.isEliminated ? "You die!" : "You win!";
        message += " Press ENTER to try again.";
        double textWidth = computeTextWidth(message, gc.getFont());
        double centerX = (CANVAS_WIDTH - textWidth) / 2;
        double centerY = CANVAS_HEIGHT / 2;
        gc.fillText(message, centerX, centerY);
    }

    private void scheduleDeathSequence(Player p, double delaySeconds) {
        // Play the gunshot sound immediately.
        PauseTransition delay = new PauseTransition(Duration.seconds(delaySeconds));
        delay.setOnFinished(event -> {
            // Randomly choose one of the two death sounds.
            if (random.nextBoolean()) {
                gunshotSound.play();
                deathSound1.play();
            } else {
                gunshotSound.play();
                deathSound2.play();
            }
        });
        delay.play();
    }

    private void drawDeadSprite(GraphicsContext gc, Player p) {
        double baseX = (p.x - 1) * CELL_SIZE;
        double baseY = p.y * CELL_SIZE + STAT_HEIGHT;
        gc.setFill(Color.GRAY); // Use gray to indicate death.
        // Draw a dead body: head becomes " X " instead of " O "
        putSafeString(gc, baseX, baseY - 2 * CELL_SIZE, "  ____");
        putSafeString(gc, baseX, baseY - 1 * CELL_SIZE, "--O---");
    }

    public static void main(String[] args) {
        launch(args);
    }
}