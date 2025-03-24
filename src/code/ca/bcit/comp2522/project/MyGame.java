package ca.bcit.comp2522.project;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

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

    // Game states
    private enum GameState { INTRO, GAME, GAME_OVER }
    private GameState gameState = GameState.INTRO;

    // Player and game state
    private static class Player {
        int x, y;
        int prevX, prevY;
        boolean isUser;
        boolean isEliminated;

        Player(int x, int y) {
            this.x = x;
            this.y = y;
            this.prevX = x;
            this.prevY = y;
            this.isUser = false;
            this.isEliminated = false;
        }
    }

    private enum Direction { UP, DOWN, LEFT, RIGHT }

    private List<Player> players;
    private Player user;
    private boolean isGreen = true;
    private long nextSwitch; // in ms
    private boolean gameOver = false;
    private final Random random = new Random();

    // Timing variables (in nanoseconds)
    private long lastUpdateTime = 0;
    private long lastLightSwitchTime = 0;
    // We will aim to update game logic roughly every 50ms
    private static final long UPDATE_INTERVAL = 50_000_000;

    // For intro screen logo (optional)
    private String[] logoLines;

    @Override
    public void start(Stage primaryStage) {
        // Set up the canvas and scene
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
            // If not found, use a default simple logo
            logoLines = new String[]{"RED LIGHT", "GREEN LIGHT"};
        }

        // Set up key input for both intro and game states
        scene.setOnKeyPressed(e -> {
            if (gameState == GameState.INTRO) {
                if (e.getCode() == KeyCode.ENTER) {
                    initGame();
                    gameState = GameState.GAME;
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    Platform.exit();
                }
            } else if (gameState == GameState.GAME) {
                // Only allow movement if the game is not over
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
                        attemptMove(user, dir);
                    }
                }
            } else if (gameState == GameState.GAME_OVER) {
                // On game over screen, allow exit with any key
                Platform.exit();
            }
        });

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFont(Font.font("Monospaced", CELL_SIZE));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        // Initialize timing for game logic updates and light switching
        nextSwitch = 2000 + random.nextInt(2000); // 2 to 4 sec in ms
        lastLightSwitchTime = System.nanoTime();

        // Create and start the game loop
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
            }
        };
        gameLoop.start();
    }

    /**
     * Loads the ASCII art logo from the resource file.
     */
    private String[] loadLogo() throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/logo.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            if (is == null) {
                throw new IOException("Logo resource not found!");
            }
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines.toArray(new String[0]);
    }

    /**
     * Initializes players and game state.
     */
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
        isGreen = true;
        gameOver = false;
        nextSwitch = 1500 + random.nextInt(2000);
        lastLightSwitchTime = System.nanoTime();
    }

    /**
     * Updates the game logic.
     */
    private void updateGame(long now) {
        // Process NPC movements based on the light color
        if (isGreen) {
            for (Player p : players) {
                if (!p.isUser && !p.isEliminated && random.nextDouble() < 0.1) {
                    attemptMove(p, Direction.UP);
                }
            }
        } else {
            for (Player p : players) {
                if (!p.isUser && !p.isEliminated && random.nextDouble() < 0.005) {
                    Direction dir = Direction.values()[random.nextInt(4)];
                    attemptMove(p, dir);
                }
            }
        }

        // Check elimination during red light using stored previous positions
        if (!isGreen) {
            for (Player p : players) {
                if (!p.isEliminated && (p.x != p.prevX || p.y != p.prevY)) {
                    p.isEliminated = true;
                    if (p.isUser) {
                        gameOver = true;
                    }
                }
            }
        }

        // Check win condition: if the user reaches the top row
        if (user.y == 0 && !user.isEliminated) {
            gameOver = true;
        }

        // Update the light timer
        long elapsedMs = (now - lastLightSwitchTime) / 1_000_000;
        if (elapsedMs >= nextSwitch) {
            isGreen = !isGreen;
            lastLightSwitchTime = now;
            nextSwitch = 1500 + random.nextInt(2000); // 1.5 to 3.5 seconds
        }

        // IMPORTANT: Update previous positions after processing all movements and checks.
        for (Player p : players) {
            p.prevX = p.x;
            p.prevY = p.y;
        }

        if (gameOver) {
            gameState = GameState.GAME_OVER;
        }
    }

    /*
     * Attempts to move a given player in the specified direction,
     * pushing other players if the space is occupied.
     */
    private void attemptMove(Player p, Direction dir) {
        int dx = 0, dy = 0;
        switch (dir) {
            case UP:    dy = -1; break;
            case DOWN:  dy = 1;  break;
            case LEFT:  dx = -1; break;
            case RIGHT: dx = 1;  break;
        }
        List<Player> visited = new ArrayList<>();
        tryMoveWithPush(p, dx, dy, visited);
    }

    /**
     * Returns the player at the given grid coordinate, or null if none.
     */
    private Player getPlayerAt(int x, int y) {
        for (Player p : players) {
            if (!p.isEliminated && p.x == x && p.y == y) {
                return p;
            }
        }
        return null;
    }

    /**
     * Draws the game state.
     */
    private void drawGame(GraphicsContext gc) {
        // Clear canvas with black background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Draw light machine (top row)
        gc.setFont(Font.font("Monospaced", CELL_SIZE));
        gc.setTextAlign(TextAlignment.LEFT);
        for (int x = 0; x < GRID_WIDTH; x++) {
            gc.setFill(isGreen ? Color.GREEN : Color.RED);
            gc.fillText("*", x * CELL_SIZE, 0);
        }

        // Draw all players
        for (Player p : players) {
            drawPlayerSprite(gc, p);
        }
    }

    /**
     * Draws a player's ASCII sprite.
     */
    private void drawPlayerSprite(GraphicsContext gc, Player p) {
        if (p.isEliminated) {
            return;
        }
        // Calculate base drawing position (we shift left one cell to allow for multi-character sprite)
        double baseX = (p.x - 1) * CELL_SIZE;
        double baseY = p.y * CELL_SIZE;
        gc.setFill(Color.WHITE);
        if (p.isUser) {
            // Draw user sprite (with "YOU" above)
            putSafeString(gc, baseX, baseY - 3 * CELL_SIZE, "YOU");
            putSafeString(gc, baseX, baseY - 2 * CELL_SIZE, " O ");
            putSafeString(gc, baseX, baseY - 1 * CELL_SIZE, "/|\\");
            putSafeString(gc, baseX, baseY,           "/ \\");
        } else {
            // Draw NPC sprite
            putSafeString(gc, baseX, baseY - 2 * CELL_SIZE, " O ");
            putSafeString(gc, baseX, baseY - 1 * CELL_SIZE, "/|\\");
            putSafeString(gc, baseX, baseY,           "/ \\");
        }
    }

    // Helper method to compute text width
    private double computeTextWidth(String text, Font font) {
        Text tempText = new Text(text);
        tempText.setFont(font);
        return tempText.getLayoutBounds().getWidth();
    }


    /**
     * Draws a string only if its position is within the canvas bounds.
     */
    private void putSafeString(GraphicsContext gc, double x, double y, String s) {
        if (x + s.length() * CELL_SIZE / 2 < 0 || x > CANVAS_WIDTH) return;
        if (y < 0 || y > CANVAS_HEIGHT) return;
        gc.fillText(s, x, y);
    }

    /**
     * Draws the intro screen with logo, title, and instructions.
     */
    private void drawIntroScreen(GraphicsContext gc) {
        // Clear background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", CELL_SIZE));

        // Draw logo (if available) centered vertically around the middle
        int logoHeight = logoLines.length;
        double logoY = CANVAS_HEIGHT / 2 - (logoHeight * CELL_SIZE) / 2 - CELL_SIZE;
        for (int i = 0; i < logoLines.length; i++) {
            String line = logoLines[i];
            double textWidth = computeTextWidth(line, gc.getFont());
            double x = (CANVAS_WIDTH - textWidth) / 2;
            gc.fillText(line, x, logoY + i * CELL_SIZE);
        }
        // Draw title with a red word in the middle
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

        // Draw instructions
        String instructions = "Press ENTER to PLAY or ESC to EXIT";
        double instrWidth = computeTextWidth(instructions, gc.getFont());
        double instrX = (CANVAS_WIDTH - instrWidth) / 2;
        double instrY = titleY + CELL_SIZE * 2;
        gc.fillText(instructions, instrX, instrY);
    }

    /**
     * Draws the game over screen with a win/lose message.
     */
    private void drawGameOverScreen(GraphicsContext gc) {
        // Clear background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", CELL_SIZE));

        String message = user.isEliminated ? "You die!" : "You win!";
        double textWidth = computeTextWidth(message, gc.getFont());
        double centerX = (CANVAS_WIDTH - textWidth) / 2;
        double centerY = CANVAS_HEIGHT / 2;
        gc.fillText(message, centerX, centerY);
    }

    // Define a simple rectangle for collision detection.
    private static class Rect {
        int x, y, width, height;
        Rect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    // Given a center coordinate and whether the player is the user,
// return the collision bounds based on the drawn sprite.
    private Rect getBoundsForPosition(int centerX, int centerY, boolean isUser) {
        int left = centerX - 1;
        // For the user, the sprite is drawn from (centerY - 3) to centerY (4 rows),
        // for NPCs from (centerY - 2) to centerY (3 rows).
        int top = isUser ? centerY - 3 : centerY - 2;
        int width = 3;
        int height = isUser ? 4 : 3;
        return new Rect(left, top, width, height);
    }

    // Check if two rectangles intersect.
    private boolean rectsIntersect(Rect r1, Rect r2) {
        return r1.x < r2.x + r2.width && r1.x + r1.width > r2.x &&
                r1.y < r2.y + r2.height && r1.y + r1.height > r2.y;
    }

    private boolean tryMoveWithPush(Player p, int dx, int dy, List<Player> visited) {
        if (visited.contains(p)) return false; // Prevent cycles
        visited.add(p);

        int newX = p.x + dx;
        int newY = p.y + dy;

        // Check if the new center is within bounds.
        if(newX < 0 || newX >= GRID_WIDTH || newY < 0 || newY >= GRID_HEIGHT) {
            return false;
        }

        Rect newBounds = getBoundsForPosition(newX, newY, p.isUser);

        // Check for collisions with other players.
        for (Player q : players) {
            if (q == p || q.isEliminated) continue;
            Rect qBounds = getBoundsForPosition(q.x, q.y, q.isUser);
            if (rectsIntersect(newBounds, qBounds)) {
                // Try to push the colliding player in the same direction.
                boolean pushed = tryMoveWithPush(q, dx, dy, visited);
                if (!pushed) {
                    return false;
                }
            }
        }
        // No collision (or successful pushes), so update p's position.
        p.x = newX;
        p.y = newY;
        return true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}