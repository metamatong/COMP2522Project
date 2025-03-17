package ca.bcit.comp2522.project;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;
    private static List<Player> players;
    private static Player user;
    private static boolean isGreen = true;
    private static long lightTimer = 0;
    private static long nextSwitch = 2000; // Initial 2 seconds
    private static boolean gameOver = false;
    private static Random random = new Random();
    private static Screen screen;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Initialize terminal and screen
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        screen = new TerminalScreen(terminal);
        screen.startScreen();

        // Initialize players
        initializePlayers();

        // Game loop
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
                    case ArrowUp: dir = Direction.UP; break;
                    case ArrowDown: dir = Direction.DOWN; break;
                    case ArrowLeft: dir = Direction.LEFT; break;
                    case ArrowRight: dir = Direction.RIGHT; break;
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
                    if (!p.isUser && !p.isEliminated && random.nextDouble() < 0.01) {
                        Direction dir = Direction.values()[random.nextInt(4)];
                        attemptMove(p, dir);
                    }
                }
            }

            // Draw the game state
            screen.clear();
            TextGraphics tg = screen.newTextGraphics();

            // Draw light machine
            for (int x = 0; x < WIDTH; x++) {
                TextCharacter greenStar = new TextCharacter('*')
                        .withForegroundColor(TextColor.ANSI.GREEN)
                        .withBackgroundColor(TextColor.ANSI.BLACK);

                tg.setCharacter(x, 0, greenStar);
            }
            // Draw players
            for (Player p : players) {
                if (!p.isEliminated) {
                    char c = p.isUser ? 'P' : 'N';

                    TextCharacter character = new TextCharacter(c)
                            .withForegroundColor(TextColor.ANSI.WHITE)
                            .withBackgroundColor(TextColor.ANSI.BLACK);

                    tg.setCharacter(p.x, p.y, character);
                }
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
                nextSwitch = 2000 + random.nextInt(3000); // 2 to 5 seconds
            }

            Thread.sleep(50); // 20 fps
        }

        // Display game over message
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();
        String message = user.isEliminated ? "You lose!" : "You win!";
        int centerX = WIDTH / 2 - (message.length() / 2);
        tg.putString(centerX, HEIGHT / 2, message);

        screen.refresh();
        Thread.sleep(2000);

        // Clean up
        screen.stopScreen();
    }

    private static void initializePlayers() {
        players = new ArrayList<>();
        List<Integer> columns = new ArrayList<>();
        for (int i = 0; i < WIDTH; i++) {
            columns.add(i);
        }
        Collections.shuffle(columns);

        for (int i = 0; i < 15; i++) {
            Player p = new Player(columns.get(i), HEIGHT - 1);
            if (i == 0) { // First player is the user
                p.isUser = true;
                user = p;
            }
            players.add(p);
        }
    }

    private static void attemptMove(Player p, Direction dir) {
        int dx = 0, dy = 0;
        switch (dir) {
            case UP: dy = -1; break;
            case DOWN: dy = 1; break;
            case LEFT: dx = -1; break;
            case RIGHT: dx = 1; break;
        }

        int newX = p.x + dx;
        int newY = p.y + dy;

        // Check bounds
        if (newX < 0 || newX >= WIDTH || newY < 0 || newY >= HEIGHT) {
            return;
        }

        // Check occupancy
        Player occupant = getPlayerAt(newX, newY);
        if (occupant == null) {
            p.x = newX;
            p.y = newY;
        } else {
            int pushX = newX + dx;
            int pushY = newY + dy;
            if (pushX >= 0 && pushX < WIDTH && pushY >= 0 && pushY < HEIGHT && getPlayerAt(pushX, pushY) == null) {
                occupant.x = pushX;
                occupant.y = pushY;
                p.x = newX;
                p.y = newY;
            }
        }
    }

    private static Player getPlayerAt(int x, int y) {
        for (Player p : players) {
            if (!p.isEliminated && p.x == x && p.y == y) {
                return p;
            }
        }
        return null;
    }
}