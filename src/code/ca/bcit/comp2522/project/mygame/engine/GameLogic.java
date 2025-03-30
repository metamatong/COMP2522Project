package ca.bcit.comp2522.project.mygame.engine;

import ca.bcit.comp2522.project.mygame.audio.SoundManager;
import ca.bcit.comp2522.project.mygame.common.MovementDirection;
import ca.bcit.comp2522.project.mygame.entities.Player;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ca.bcit.comp2522.project.mygame.common.GameConfig.FINISH_LINE_Y;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.GRID_HEIGHT;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.GRID_WIDTH;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.MOVE_COOLDOWN_NANOSECONDS;
import static ca.bcit.comp2522.project.mygame.util.DrawingUtils.directionDeltaX;
import static ca.bcit.comp2522.project.mygame.util.DrawingUtils.directionDeltaY;

public class GameLogic
{
    private List<Player> players;
    private SoundManager soundManager;
    private final Random random = new Random();
    private Player user;
    private long gameStartTime;
    private int finishedCount = 0;
    private boolean fieldCleared = false; // once set, remaining players are eliminated
    private boolean green = true;
    private boolean gameOver = false;
    private long nextSwitch; // in ms
    private long lastLightSwitchTime = 0;

    public GameLogic(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getUser()
    {
        return user;
    }

    public long getGameStartTime() {
        return gameStartTime;
    }

    public int getFinishedCount() {
        return finishedCount;
    }

    public boolean isGreen()
    {
        return green;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public long getNextSwitch() {
        return nextSwitch;
    }

    public long getLastLightSwitchTime() {
        return lastLightSwitchTime;
    }

    public void setNextSwitch(long nextSwitch) {
        this.nextSwitch = nextSwitch;
    }

    public void setLastLightSwitchTime(long lastLightSwitchTime) {
        this.lastLightSwitchTime = lastLightSwitchTime;
    }

    // Initializes the game state.
    public void initGame() {
        players = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            int startX = random.nextInt(GRID_WIDTH);
            Player p = new Player(startX, GRID_HEIGHT - 1);
            if (i == 0) {
                p.setUser(true);
                user = p;
            }
            players.add(p);
        }

        gameStartTime = System.nanoTime();
        // Reset finishing fields.
        finishedCount = 0;
        fieldCleared = false;
        green = true;
        gameOver = false;
        nextSwitch = 1500 + random.nextInt(2000);
        lastLightSwitchTime = System.nanoTime();
    }

    // Updates game logic.
    public void updateGame(long now) {
        // Process NPC voluntary moves.
        if (green) {
            for (Player p : players) {
                if (!p.isEliminated() && !p.isUser() && random.nextDouble() < 0.1) {
                    tryMoveWithPush(p, 0, -1, new ArrayList<>(), true);
                }
            }
        } else {
            // During red light, occasional moves.
            for (Player p : players) {
                if (!p.isEliminated() && !p.isUser() && random.nextDouble() < 0.001) {
                    MovementDirection d = MovementDirection.values()[random.nextInt(4)];
                    tryMoveWithPush(p, directionDeltaX(d), directionDeltaY(d), new ArrayList<>(), true);
                }
            }
        }

        // --- FINISH LINE LOGIC ---
        // Process finishers: any player at y==0 (finish line) and not yet finished.
        for (Player p : players) {
            if (!p.isEliminated() && !p.isFinished() && p.getY() < FINISH_LINE_Y) {
                if (finishedCount < 10) {
                    p.setFinished(true);
                    finishedCount++;
                } else {
                    // Finished too late: eliminate this player.
                    p.setEliminated(true);
                    p.setDeathTimeStamp(now);
                    playDeathSequence();
                }
            }
        }
        // If 10 players have finished, then eliminate everyone else.
        if (finishedCount >= 10 && !fieldCleared) {
            int index = 0;
            for (Player p : players) {
                if (!p.isFinished() && !p.isEliminated()) {
                    p.setEliminated(true);
                    p.setDeathTimeStamp(now);
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
        if (!green) {
            for (Player p : players) {
                if (!p.isEliminated() && !p.isFinished() && (p.getX() != p.getPrevX() || p.getY() != p.getPrevY())) {
                    p.setEliminated(true);
                    p.setDeathTimeStamp(now);
                    playDeathSequence();
                    if (p.isUser()) gameOver = true;
                }
            }
        }

        // Win condition: if the user has finished.
        if (user.getY() <= FINISH_LINE_Y && !user.isEliminated()) {
            // The user finished.
            user.setFinished(true);
            gameOver = true;
        }

        // Update light timer.
        long elapsedMs = (now - lastLightSwitchTime) / 1_000_000;
        if (elapsedMs >= nextSwitch) {
            green = !green;
            lastLightSwitchTime = now;
            // Longer red light duration.
            if (green) {
                nextSwitch = 1500 + random.nextInt(2000);
            } else {
                nextSwitch = 3000 + random.nextInt(2000);
            }
        }

        // Update previous positions.
        for (Player p : players) {
            p.setPrevX(p.getX());
            p.setPrevY(p.getY());
        }
    }

    /*
     * Attempts to move player p by (dx, dy).
     * For voluntary (initiating) moves, if the destination cell is occupied,
     * p does not change its own cell (thus staying hidden) but attempts to push the occupant.
     * For forced moves (initiating == false), the player always moves.
     * If a push is initiated by the user, a sound effect is played.
     */
    public boolean tryMoveWithPush(Player p, int dx, int dy, List<Player> visited, boolean initiating) {
        long now = System.nanoTime();
        // Only allow the move if the cooldown has elapsed
        if (now - p.getLastMoveTime() < MOVE_COOLDOWN_NANOSECONDS) {
            return false;
        }

        if (visited.contains(p)) return false;
        visited.add(p);
        int newX = p.getX() + dx;
        int newY = p.getY() + dy;
        if (newX < 0 || newX >= GRID_WIDTH || newY < 0 || newY >= GRID_HEIGHT) return false;
        Player occupant = getPlayerAt(newX, newY);
        if (occupant != null) {
            if (initiating && p.isUser()) {
                soundManager.playPushSound();
                // Set visual flags for pushing and pushed.
                p.setPushing(true);
                occupant.setPushed(true);
                // Reset these flags after a short delay.
                PauseTransition pt = new PauseTransition(Duration.millis(300));
                pt.setOnFinished(e -> {
                    p.setPushing(false);
                    occupant.setPushed(false);
                });
                pt.play();
            }
            int oldX = p.getX();
            int oldY = p.getY();
            boolean pushed = tryMoveWithPush(occupant, dx, dy, visited, false);
            if (!pushed) return false;
            if (initiating) {
                p.setX(oldX);
                p.setY(oldY);
            } else {
                p.setX(newX);
                p.setY(newY);
            }
            p.setLastMoveTIme(now);
            return true;
        } else {
            p.setX(newX);
            p.setY(newY);
            p.setLastMoveTIme(now);
            return true;
        }
    }

    // Returns the player occupying the given grid cell, or null.
    private Player getPlayerAt(int x, int y) {
        for (Player p : players) {
            if (!p.isEliminated() && p.getX() == x && p.getY() == y) {
                return p;
            }
        }
        return null;
    }


    // Plays the death sound sequence: gunshot immediately, then (after 0.5s) a random death sound.
    private void playDeathSequence() {
        soundManager.playGunshotSound();
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        delay.setOnFinished(event -> {
            if (random.nextBoolean()) {
                soundManager.playDeathSound1();
            } else {
                soundManager.playDeathSound2();
            }
        });
        delay.play();
    }

    private void scheduleDeathSequence(Player p, double delaySeconds) {
        // Play the gunshot sound immediately.
        PauseTransition delay = new PauseTransition(Duration.seconds(delaySeconds));
        delay.setOnFinished(event -> {
            // Randomly choose one of the two death sounds.
            if (random.nextBoolean()) {
                soundManager.playGunshotSound();
                soundManager.playDeathSound1();
            } else {
                soundManager.playGunshotSound();
                soundManager.playDeathSound2();
            }
        });
        delay.play();
    }
}
