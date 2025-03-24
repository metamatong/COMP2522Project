package ca.bcit.comp2522.project;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RedLightGreenLight {

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

    private static final int WIDTH = 60;  // bigger than 20
    private static final int HEIGHT = 25; // bigger than 20
    private static int xOffset = 0; // Where to start drawing in the terminal (left offset)
    private static int yOffset = 0; // Where to start drawing in the terminal (top offset)

    private static List<Player> players;
    private static Player user;
    private static boolean isGreen = true;
    private static long lightTimer = 0;
    private static long nextSwitch = 2000; // Initial 2 seconds
    private static boolean gameOver = false;
    private static final Random random = new Random();
    private static Screen screen;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Initialize terminal and screen
        Terminal terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(120, 40))
                .createTerminal();
        screen = new TerminalScreen(terminal);
        screen.startScreen();

        // Show intro or menu
        boolean wantsToPlay = showIntro();
        if (!wantsToPlay) {
            // User chose to exit or not start
            screen.stopScreen();
            return;
        }

        // CENTERING CHANGES: Now that the user is starting the game, figure out the offsets
        TerminalSize size = screen.getTerminalSize(); // e.g. 120 x 40
        int columns = size.getColumns();
        int rows = size.getRows();
        xOffset = (columns - WIDTH) / 2;  // center horizontally
        yOffset = (rows - HEIGHT) / 2;    // center vertically

        // Now actually start the game
        initializePlayers();
        runGameLoop();
    }

    /*
     * Loads the ASCII art logo from the resource file.
     *
     * @return an array of strings, each representing a line of the logo.
     * @throws IOException if the file cannot be read.
     */
    private static String[] loadLogo() throws IOException {
        List<String> lines = new ArrayList<>();
        // Use the class loader to get the resource as an InputStream.
        try (InputStream is = RedLightGreenLight.class.getResourceAsStream("/logo.txt");
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
     * Displays a simple intro screen with a 'Play' prompt.
     * Returns true if the user wants to start the game, false if they want to exit.
     */
    private static boolean showIntro() throws IOException {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();

        TerminalSize size = screen.getTerminalSize();
        int columns = size.getColumns(); // 120
        int rows = size.getRows();       // 40

        // Load the ASCII art logo from the resource file.
        String[] logo = loadLogo();

        // Determine where to position the title.
        int titleY = rows / 2 + 15;
        // Calculate the height of the logo and set its starting Y coordinate
        int logoHeight = logo.length;
        int logoStartY = titleY - logoHeight - 1; // leave one empty line between logo and title

        // Draw the ASCII art logo, centering each line horizontally.
        for (int i = 0; i < logo.length; i++) {
            String line = logo[i];
            int lineX = columns / 2 - line.length() / 2;
            int lineY = logoStartY + i;
            tg.putString(lineX, lineY, line);
        }

        String titlePart1 = "WELCOME TO RED LIGHT ";
        String titlePart2 = " LIGHT";
        // Calculate total length for center offset
        String combinedForLength = titlePart1 + "BLOOD" + titlePart2;
        int totalLength = combinedForLength.length();

        int titleX = columns / 2 - totalLength / 2;

        // Print "WELCOME TO RED LIGHT "
        tg.setForegroundColor(TextColor.ANSI.DEFAULT);
        tg.putString(titleX, titleY, titlePart1);

        // Print "BLOOD" in red, immediately after
        tg.setForegroundColor(TextColor.ANSI.RED);
        tg.putString(titleX + titlePart1.length(), titleY, "BLOOD");

        // Then the last part
        tg.setForegroundColor(TextColor.ANSI.DEFAULT);
        tg.putString(titleX + titlePart1.length() + "BLOOD".length(), titleY, titlePart2);

        String instructions = "Press ENTER to PLAY or ESC to EXIT";

        // Center the instructions below the title.
        int instructionsX = columns / 2 - instructions.length() / 2;
        int instructionsY = titleY + 2;
        tg.putString(instructionsX, instructionsY, instructions);

        screen.refresh();

        // Now wait for Enter or Escape
        while (true) {
            KeyStroke keyStroke = screen.readInput();
            if (keyStroke != null) {
                if (keyStroke.getKeyType() == KeyType.Enter) {
                    return true;
                } else if (keyStroke.getKeyType() == KeyType.Escape) {
                    return false;
                }
            }
        }
    }

    /*
     * The main game loop logic.
     */
    private static void runGameLoop() throws IOException, InterruptedException {
        while (!gameOver) {
            // Set previous positions
            for (Player p : players) {
                p.prevX = p.x;
                p.prevY = p.y;
            }

            // Handle user input
            KeyStroke key = screen.pollInput();
            if (key != null) {
                Direction dir = null;
                switch (key.getKeyType()) {
                    case ArrowUp:
                        dir = Direction.UP;
                        break;
                    case ArrowDown:
                        dir = Direction.DOWN;
                        break;
                    case ArrowLeft:
                        dir = Direction.LEFT;
                        break;
                    case ArrowRight:
                        dir = Direction.RIGHT;
                        break;
                }
                if (dir != null) {
                    attemptMove(user, dir);
                }
            }

            // Update NPC movements
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

            // Draw the game state
            screen.clear();
            TextGraphics tg = screen.newTextGraphics();

            // Draw light machine at the top
            for (int x = 0; x < WIDTH; x++) {
                // If isGreen is true, draw green, otherwise draw red
                TextCharacter starChar = new TextCharacter('*')
                        .withForegroundColor(isGreen ? TextColor.ANSI.GREEN : TextColor.ANSI.RED)
                        .withBackgroundColor(TextColor.ANSI.BLACK);

                tg.setCharacter(xOffset + x, yOffset + 0, starChar);
            }

            // Draw players
            for (Player p : players) {
                drawPlayerSprite(tg, p);
            }

            screen.refresh();

            // Check eliminations during red light
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

            // Check win condition
            if (user.y == 0 && !user.isEliminated) {
                gameOver = true;
            }

            // Update light timer
            lightTimer += 50;
            if (lightTimer >= nextSwitch) {
                isGreen = !isGreen;
                lightTimer = 0;
                nextSwitch = 1500 + random.nextInt(2000); // 1.5 to 3.5 seconds
            }

            Thread.sleep(50); // ~20 fps
        }

        // Display game over message
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();
        String message = user.isEliminated ? "You die!" : "You win!";
        int centerX = xOffset + (WIDTH / 2) - (message.length() / 2);
        int centerY = yOffset + (HEIGHT / 2);
        tg.putString(centerX, centerY, message);

        screen.refresh();
        Thread.sleep(2000);

        // Clean up
        screen.stopScreen();
    }

    /*
     * Initializes our player list and positions them at the bottom.
     */
    private static void initializePlayers() {
        players = new ArrayList<>();

        for (int i = 0; i < 45; i++) {
            // Place new player in a random column at the bottom row
            int startX = random.nextInt(WIDTH);
            Player p = new Player(startX, HEIGHT - 1);

            if (i == 0) { // First player is the user
                p.isUser = true;
                user = p;
            }
            players.add(p);
        }
    }

    /*
     * Attempts to move a given player in the specified direction,
     * also pushing other players if the space is occupied.
     */
    private static void attemptMove(Player p, Direction dir) {
        int dx = 0, dy = 0;
        switch (dir) {
            case UP:
                dy = -1;
                break;
            case DOWN:
                dy = 1;
                break;
            case LEFT:
                dx = -1;
                break;
            case RIGHT:
                dx = 1;
                break;
        }

        int newX = p.x + dx;
        int newY = p.y + dy;

        // Check bounds
        if (newX < 0 || newX >= WIDTH || newY < 0 || newY >= HEIGHT) {
            return;
        }

        // Check if position is already occupied
        Player occupant = getPlayerAt(newX, newY);
        if (occupant == null) {
            p.x = newX;
            p.y = newY;
        } else {
            // Attempt to push occupant
            int pushX = occupant.x + dx;
            int pushY = occupant.y + dy;
            if (pushX >= 0 && pushX < WIDTH && pushY >= 0 && pushY < HEIGHT && getPlayerAt(pushX, pushY) == null) {
                occupant.x = pushX;
                occupant.y = pushY;
                p.x = newX;
                p.y = newY;
            }
        }
    }

    /*
     * Returns the player occupying the position (x,y), or null if nobody is there.
     */
    private static Player getPlayerAt(int x, int y) {
        for (Player p : players) {
            if (!p.isEliminated && p.x == x && p.y == y) {
                return p;
            }
        }
        return null;
    }

    /*
     * Draws the player's ASCII sprite if they are not eliminated.
     */
    private static void drawPlayerSprite(TextGraphics tg, Player p) {
        if (p.isEliminated) {
            return;
        }
        // We'll offset in the drawing
        int baseX = xOffset + (p.x - 1);
        int baseY = yOffset + p.y;

        if (p.isUser) {
            putSafeString(tg, baseX, baseY - 3, "YOU");
            putSafeString(tg, baseX, baseY - 2, " O ");
            putSafeString(tg, baseX, baseY - 1, "/|\\");
            putSafeString(tg, baseX, baseY,     "/ \\");
        } else {
            putSafeString(tg, baseX, baseY - 2, " O ");
            putSafeString(tg, baseX, baseY - 1, "/|\\");
            putSafeString(tg, baseX, baseY,     "/ \\");
        }
    }

    private static void putSafeString(TextGraphics tg, int x, int y, String s) {
        // If it's outside the screen bounds, skip
        TerminalSize termSize = screen.getTerminalSize();
        if (y < 0 || y >= termSize.getRows()) {
            return;
        }
        if (x < 0 || x + s.length() >= termSize.getColumns()) {
            return;
        }
        tg.putString(x, y, s);
    }
}