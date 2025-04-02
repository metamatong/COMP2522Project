package ca.bcit.comp2522.project.mygame.engine;

import ca.bcit.comp2522.project.mygame.audio.SoundManager;
import ca.bcit.comp2522.project.mygame.common.MovementDirection;
import ca.bcit.comp2522.project.mygame.entities.Player;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ca.bcit.comp2522.project.mygame.common.GameConfig.ALLOWED_NUMBER_OF_WINNERS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.BOTTOM_OFFSET_IN_NUMBER_OF_CELLS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.FINISH_LINE_Y_IN_NUMBER_OF_CELLS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.GRID_HEIGHT_IN_NUMBER_OF_CELLS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.GRID_WIDTH_IN_NUMBER_OF_CELLS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.LIGHT_SWITCH_FROM_RED_LIGHT_MINIMUM_INTERVAL_IN_MILLISECONDS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.LIGHT_SWITCH_MINIMUM_INTERVAL_IN_MILLISECONDS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.LIGHT_SWITCH_TIME_VARIABILITY_IN_MILLISECONDS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.MOVE_COOLDOWN_IN_NANOSECONDS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.NPC_MOVING_DISTANCE_ON_X_AXIS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.NPC_MOVING_DISTANCE_ON_Y_AXIS;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.NPC_MOVING_IN_RED_LIGHT_PROBABILITY;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.NPC_MOVING_SPEED_PROBABILITY;
import static ca.bcit.comp2522.project.mygame.common.GameConfig.NUMBER_OF_PLAYERS;
import static ca.bcit.comp2522.project.mygame.util.DrawingUtils.directionDeltaX;
import static ca.bcit.comp2522.project.mygame.util.DrawingUtils.directionDeltaY;

/**
 * Handles the core game logic and mechanics for updating game state, processing player movements,
 * finish line and red light logic, and managing sound effects.
 * <p>
 * This class is responsible for initializing the game, updating the positions and states of players,
 * processing voluntary and forced moves (including push logic), and determining win and elimination conditions.
 * </p>
 * @author Kyle Cheon
 * @version 1.0
 */
public class GameLogic
{
    private static GameLogic singleGameLogic; // This variable stores single instance of GameLogic (Singleton pattern)

    private static final int FIRST_INDEX = 0;
    private static final int INITIAL_COUNT = 0;
    private static final boolean INITIAL_LIGHT_CONDITION = true;
    private static final int NUMBER_OF_MOVEMENT_DIRECTIONS = 4;
    private static final double BASE_SOUND_DELAY = 0.5;
    private static final double SOUND_DELAY_WEIGHT = 0.05;
    private static final int NANOSECOND_PER_MILLISECOND = 1000000;
    private static final int PUSHING_DELAY_IN_MILLISECOND = 300;

    private final SoundManager soundManager;
    private final Random random = new Random();
    private Player user;
    private boolean gameOver = false;
    private long gameStartTimeInNanoseconds;

    // List of all players in the game.
    private List<Player> players;

    // Count of players who have successfully finished.
    private int finishedCount = 0;

    // Flag indicating whether the game field has been cleared (remaining players eliminated).
    private boolean fieldCleared = false;

    // Flag representing the current state of the light (green or red).
    private boolean green = true;

    // Time in milliseconds until the next light switch.
    private long nextSwitchInMilliseconds;

    // Timestamp (in nanoseconds) of the last light switch.
    private long lastLightSwitchTimeInNanoseconds = 0;

    static
    {
        singleGameLogic = null;
    }

    /*
     * Constructs a new GameLogic instance with the specified SoundManager.
     *
     * @param soundManager the SoundManager used to play game sound effects.
     */
    private GameLogic(final SoundManager soundManager)
    {
        validateSoundManager(soundManager);
        this.soundManager = soundManager;
    }

    /**
     * This is a method calling private constructor of GameLogic class
     * to ensure there is only one instance of GameLogic object.
     *
     * @param soundManager
     * @return
     */
    public static GameLogic getInstance(final SoundManager soundManager)
    {
        if(singleGameLogic == null)
        {
            singleGameLogic = new GameLogic(soundManager);
        }
        return singleGameLogic;
    }

    /**
     * Returns the list of players currently in the game.
     *
     * @return the list of players.
     */
    public List<Player> getPlayers()
    {
        return players;
    }

    /**
     * Returns the user-controlled player.
     *
     * @return the user player.
     */
    public Player getUser()
    {
        return user;
    }

    /**
     * Returns the game start time in nanoseconds.
     *
     * @return the start time of the game.
     */
    public long getGameStartTimeInNanoseconds()
    {
        return gameStartTimeInNanoseconds;
    }

    /**
     * Returns the number of players that have finished the game.
     *
     * @return the finished players count.
     */
    public int getFinishedCount()
    {
        return finishedCount;
    }

    /**
     * Indicates whether the game is currently in the green light state.
     *
     * @return {@code true} if the light is green; {@code false} otherwise.
     */
    public boolean isGreen()
    {
        return green;
    }

    /**
     * Indicates whether the game is over.
     *
     * @return {@code true} if the game is over; {@code false} otherwise.
     */
    public boolean isGameOver()
    {
        return gameOver;
    }

    /**
     * Returns the time in milliseconds until the next light switch.
     *
     * @return the next light switch duration in milliseconds.
     */
    public long getNextSwitchInMilliseconds()
    {
        return nextSwitchInMilliseconds;
    }

    /**
     * Returns the timestamp (in nanoseconds) of the last light switch.
     *
     * @return the last light switch time.
     */
    public long getLastLightSwitchTimeInNanoseconds()
    {
        return lastLightSwitchTimeInNanoseconds;
    }

    /**
     * Sets the duration (in milliseconds) until the next light switch.
     *
     * @param nextSwitchInMilliseconds the next switch duration in milliseconds.
     */
    public void setNextSwitchInMilliseconds(final long nextSwitchInMilliseconds)
    {
        this.nextSwitchInMilliseconds = nextSwitchInMilliseconds;
    }

    /**
     * Sets the timestamp for the last light switch.
     *
     * @param lastLightSwitchTimeInNanoseconds the new timestamp in nanoseconds.
     */
    public void setLastLightSwitchTimeInNanoseconds(final long lastLightSwitchTimeInNanoseconds)
    {
        this.lastLightSwitchTimeInNanoseconds = lastLightSwitchTimeInNanoseconds;
    }

    /**
     * Initializes the game state by creating players, resetting game variables, and setting the initial timing
     * for light switches.
     * <p>
     * This method creates 45 players with random starting positions along the bottom row of the grid.
     * The first player is designated as the user-controlled player.
     * It also resets counters, flags, and timing values for the start of a new game.
     * </p>
     */
    public void initGame()
    {
        // Create list of players and position them in a random place.
        players = new ArrayList<>();
        for(int i = 0; i < NUMBER_OF_PLAYERS; i++)
        {
            final int startX;
            final Player p;

            startX = random.nextInt(GRID_WIDTH_IN_NUMBER_OF_CELLS);
            p = new Player(startX, GRID_HEIGHT_IN_NUMBER_OF_CELLS - BOTTOM_OFFSET_IN_NUMBER_OF_CELLS);

            if(i == FIRST_INDEX)
            {
                p.setUser(true);
                user = p;
            }
            players.add(p);
        }

        // Record game starting time.
        gameStartTimeInNanoseconds = System.nanoTime();

        // Set game logic variables to its initial value.
        finishedCount = INITIAL_COUNT;
        fieldCleared = false;
        green = INITIAL_LIGHT_CONDITION;
        gameOver = false;

        // Determine when the light will change from green to red.
        nextSwitchInMilliseconds = LIGHT_SWITCH_MINIMUM_INTERVAL_IN_MILLISECONDS +
                                   random.nextInt(LIGHT_SWITCH_TIME_VARIABILITY_IN_MILLISECONDS);

        // Record last light switching time.
        lastLightSwitchTimeInNanoseconds = System.nanoTime();
    }

    /**
     * Updates the game state based on the current time.
     * <p>
     * This method processes NPC voluntary moves during green light and occasional moves during red light.
     * It also handles finish line logic by marking players who reach the finish line, eliminates players who
     * finish too late, and clears the field once the required number of players have finished.
     * Additionally, it enforces red light rules by eliminating players who move during red light, checks the win
     * condition for the user, and toggles the light state based on elapsed time.
     * </p>
     *
     * @param now the current time in nanoseconds.
     */
    public void updateGame(final long now)
    {
        // --- NPC MOVEMENT LOGIC ---
        if(green)
        {
            for(final Player p : players)
            {
                if(!p.isEliminated() && !p.isUser() && random.nextDouble() < NPC_MOVING_SPEED_PROBABILITY)
                {
                    tryMoveWithPush(p,
                                    NPC_MOVING_DISTANCE_ON_X_AXIS,
                                    NPC_MOVING_DISTANCE_ON_Y_AXIS,
                                    new ArrayList<>(),
                                    true);
                }
            }
        }
        else
        {
            // During red light, NPC can still occasionally move with low possibility.
            for(final Player p : players)
            {
                if(!p.isEliminated() && !p.isUser() && random.nextDouble() < NPC_MOVING_IN_RED_LIGHT_PROBABILITY)
                {
                    final MovementDirection d;
                    d = MovementDirection.values()[random.nextInt(NUMBER_OF_MOVEMENT_DIRECTIONS)];
                    tryMoveWithPush(p,
                                    directionDeltaX(d),
                                    directionDeltaY(d),
                                    new ArrayList<>(),
                                    true);
                }
            }
        }

        // Red light elimination: if a player moves during red light, and they are not finished, eliminate them.
        if(!green)
        {
            for(final Player p : players)
            {
                final boolean playerMoved;
                playerMoved = (p.getX() != p.getPrevX() || p.getY() != p.getPrevY());

                if(!p.isEliminated() && !p.isFinished() && playerMoved)
                {
                    p.setEliminated(true);
                    p.setDeathTimeStamp(now);
                    playDeathSequence();
                    if(p.isUser()) gameOver = true;
                }
            }
        }

        // --- FINISH LINE LOGIC ---
        for(final Player p : players)
        {
            // Process finishers: any player at y==0 (finish line) and not yet finished.
            if(!p.isEliminated() && !p.isFinished() && p.getY() < FINISH_LINE_Y_IN_NUMBER_OF_CELLS)
            {
                if(finishedCount < ALLOWED_NUMBER_OF_WINNERS)
                {
                    p.setFinished(true);
                    finishedCount++;
                }
                else
                {
                    // Finished too late: eliminate this player.
                    p.setEliminated(true);
                    p.setDeathTimeStamp(now);
                    playDeathSequence();
                }
            }
        }
        // If ALLOWED_NUMBER_OF_WINNERS players have finished, then eliminate everyone else.
        if(finishedCount >= ALLOWED_NUMBER_OF_WINNERS && !fieldCleared)
        {
            int index;
            index = FIRST_INDEX;
            for(final Player p : players)
            {
                if(!p.isFinished() && !p.isEliminated())
                {
                    p.setEliminated(true);
                    p.setDeathTimeStamp(now);
                    // Schedule each death sequence with an increasing delay.
                    scheduleDeathSequence(BASE_SOUND_DELAY + index * SOUND_DELAY_WEIGHT);
                    index++;
                }
            }
            fieldCleared = true;
            gameOver = true;
        }

        // --- WIN CONDITION LOGIC ---
        if(user.getY() <= FINISH_LINE_Y_IN_NUMBER_OF_CELLS && !user.isEliminated())
        {
            // The user finished.
            user.setFinished(true);
            gameOver = true;
        }

        // --- LIGHT TIMER UPDATE LOGIC ---
        final long elapsedMs;
        elapsedMs = (now - lastLightSwitchTimeInNanoseconds) / NANOSECOND_PER_MILLISECOND;
        if(elapsedMs >= nextSwitchInMilliseconds)
        {
            green = !green;
            lastLightSwitchTimeInNanoseconds = now;
            if(green)
            {
                nextSwitchInMilliseconds = LIGHT_SWITCH_MINIMUM_INTERVAL_IN_MILLISECONDS +
                                           random.nextInt(LIGHT_SWITCH_TIME_VARIABILITY_IN_MILLISECONDS);
            }
            else
            {
                nextSwitchInMilliseconds = LIGHT_SWITCH_FROM_RED_LIGHT_MINIMUM_INTERVAL_IN_MILLISECONDS +
                                           random.nextInt(LIGHT_SWITCH_TIME_VARIABILITY_IN_MILLISECONDS);
            }
        }

        // --- POSITION UPDATE LOGIC ---
        for(final Player p : players)
        {
            p.setPrevX(p.getX());
            p.setPrevY(p.getY());
        }
    }

    /**
     * Attempts to move player p by (dx, dy).
     * For voluntary (initiating) moves, if the destination cell is occupied,
     * p does not change its own cell (thus staying hidden) but attempts to push the occupant.
     * For forced moves (initiating == false), the player always moves.
     * If a push is initiated by the user, a sound effect is played.
     */
    public boolean tryMoveWithPush(final Player p,
                                   final int dx,
                                   final int dy,
                                   final List<Player> visited,
                                   final boolean initiating)
    {
        final long now;
        now = System.nanoTime();

        // Allow the move only if the cooldown has elapsed.
        if(now - p.getLastMoveTimestampInNanoseconds() < MOVE_COOLDOWN_IN_NANOSECONDS)
        {
            return false;
        }

        // This line prevents players pushing each other endlessly
        if(visited.contains(p)) return false;

        visited.add(p);

        final int newX;
        final int newY;

        newX = p.getX() + dx;
        newY = p.getY() + dy;

        // This makes sure the player does not move outside the game grid.
        if(newX < FIRST_INDEX ||
           newX >= GRID_WIDTH_IN_NUMBER_OF_CELLS ||
           newY < FIRST_INDEX ||
           newY >= GRID_HEIGHT_IN_NUMBER_OF_CELLS)
        {
            return false;
        }

        final Player occupant;
        occupant = getPlayerAt(newX, newY);

        if(occupant != null)
        {
            if (initiating && p.isUser())
            {
                soundManager.playPushSound();
                // Set visual flags for pushing and pushed.
                p.setPushing(true);
                occupant.setPushed(true);
                // Reset these flags after a short delay.
                final PauseTransition pt;
                pt = new PauseTransition(Duration.millis(PUSHING_DELAY_IN_MILLISECOND));
                pt.setOnFinished(e ->
                {
                    p.setPushing(false);
                    occupant.setPushed(false);
                });
                pt.play();
            }
            final int oldX;
            final int oldY;

            oldX = p.getX();
            oldY = p.getY();

            final boolean pushed;
            pushed = tryMoveWithPush(occupant, dx, dy, visited, false);
            if (!pushed) return false;

            if (initiating)
            {
                p.setX(oldX);
                p.setY(oldY);
            }
            else
            {
                p.setX(newX);
                p.setY(newY);
            }

            p.setLastMoveTimestampInNanoseconds(now);

            return true;
        }
        else
        {
            p.setX(newX);
            p.setY(newY);
            p.setLastMoveTimestampInNanoseconds(now);
            return true;
        }
    }

    /*
     * Returns the player occupying the specified grid cell.
     *
     * @param x the x-coordinate of the grid cell.
     * @param y the y-coordinate of the grid cell.
     * @return the player at the specified cell, or {@code null} if no non-eliminated player occupies it.
     */
    private Player getPlayerAt(final int x,
                               final int y)
    {
        for(final Player p : players)
        {
            if(!p.isEliminated() && p.getX() == x && p.getY() == y)
            {
                return p;
            }
        }
        return null;
    }


    /*
     * Plays the death sequence sound effect.
     * <p>
     * This method plays a gunshot sound immediately, then after a 0.5-second delay,
     * plays one of two possible death sounds chosen at random.
     * </p>
     */
    private void playDeathSequence()
    {
        soundManager.playGunshotSound();
        final PauseTransition delay;
        delay = new PauseTransition(Duration.seconds(BASE_SOUND_DELAY));
        delay.setOnFinished(event ->
        {
            if(random.nextBoolean())
            {
                soundManager.playDeathSound1();
            }
            else
            {
                soundManager.playDeathSound2();
            }
        });
        delay.play();
    }


    /*
     * Schedules a death sequence sound effect for a player after a specified delay.
     * <p>
     * This method plays the gunshot sound immediately followed by one of two random death sounds after the delay.
     * </p>
     *
     * @param p            the player for whom the death sequence is scheduled.
     * @param delaySeconds the delay in seconds before playing the death sequence.
     */
    private void scheduleDeathSequence(final double delaySeconds)
    {
        final PauseTransition delay;
        delay = new PauseTransition(Duration.seconds(delaySeconds));

        delay.setOnFinished(event ->
        {
            // Randomly choose one of the two death sounds.
            if(random.nextBoolean())
            {
                soundManager.playGunshotSound();
                soundManager.playDeathSound1();
            }
            else
            {
                soundManager.playGunshotSound();
                soundManager.playDeathSound2();
            }
        });
        delay.play();
    }

    /*
     * Validates that the provided SoundManager is not null.
     *
     * @param soundManager the SoundManager instance to validate.
     * @throws IllegalArgumentException if soundManager is null.
     */
    private static void validateSoundManager(final SoundManager soundManager)
    {
        if(soundManager == null)
        {
            throw new IllegalArgumentException("SoundManager cannot be null.");
        }
    }
}
