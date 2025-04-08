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
 * Manages the core game logic and mechanics that drive the gameplay.
 * <p>
 * This class is responsible for initializing the game state, processing player movements (both voluntary and forced),
 * enforcing game rules such as red light conditions and finish line validations, and handling game events like push
 * actions and sound effect triggers. It employs the Singleton design pattern to ensure that only one instance of the
 * game logic exists during runtime.
 * </p>
 * <p>
 * The main responsibilities of this class include:
 * <ul>
 *   <li>Creating and positioning players at the start of the game.</li>
 *   <li>Updating the positions of players based on game events and time elapsed.</li>
 *   <li>Handling voluntary movements and forced movements with push logic when cells are occupied.</li>
 *   <li>Managing the light states (green/red) and enforcing red light rules.</li>
 *   <li>Processing win and elimination conditions, including finish line checks and timed eliminations.</li>
 *   <li>Triggering sound effects for various game events (pushes, gunshots, death sequences).</li>
 * </ul>
 * </p>
 *
 * @param <T> a type that extends {@link Player}, representing the participants in the game.
 * @author Kyle Cheon
 * @version 1.0
 */
public class GameLogic<T extends Player>
{
    private static GameLogic<?> singleGameLogic;     // Singleton instance of GameLogic.
    private List<T> players;
    private T user;

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
    private boolean gameOver = false;
    private long gameStartTimeInNanoseconds;

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
     * Private constructor that initializes the GameLogic with the specified SoundManager.
     * <p>
     * Validates the provided SoundManager to ensure sound effects can be played during game events.
     * </p>
     *
     * @param soundManager the SoundManager instance used to play game sound effects.
     * @throws IllegalArgumentException if {@code soundManager} is null.
     */
    private GameLogic(final SoundManager soundManager)
    {
        validateSoundManager(soundManager);
        this.soundManager = soundManager;
    }

    /**
     * Returns the singleton instance of the GameLogic.
     * <p>
     * This method follows the Singleton design pattern to ensure only one instance of GameLogic exists
     * during the application's lifecycle. If an instance does not exist, it is created using the provided
     * SoundManager; subsequent calls will return the same instance.
     * </p>
     *
     * @param soundManager the SoundManager to be used for initializing GameLogic.
     * @param <T>          the type of Player used in the game.
     * @return the singleton instance of GameLogic.
     */
    public static <T extends Player> GameLogic<T> getInstance(final SoundManager soundManager)
    {
        if(singleGameLogic == null)
        {
            singleGameLogic = new GameLogic<T>(soundManager);
        }
        return (GameLogic<T>) singleGameLogic;
    }

    /**
     * Returns the list of players currently in the game.
     *
     * @return the list of players.
     */
    public List<T> getPlayers()
    {
        return players;
    }


    /**
     * Returns the user-controlled player.
     *
     * @return the user player.
     */
    public T getUser()
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
     * Initializes the game state by creating and positioning players, and resetting all necessary game variables.
     * <p>
     * This method performs the following key operations:
     * <ul>
     *   <li><b>Player Creation and Positioning:</b> Generates a list of players using {@code NUMBER_OF_PLAYERS} as
     *       the total count. For each player, a random starting x-coordinate is assigned within the grid's width.
     *       All players start on the bottom row, determined by {@code GRID_HEIGHT_IN_NUMBER_OF_CELLS} minus
     *       {@code BOTTOM_OFFSET_IN_NUMBER_OF_CELLS}. The first player created is marked as the user-controlled
     *       player.</li>
     *   <li><b>Game Timing Initialization:</b> Captures the current system time in nanoseconds as the game start time,
     *       ensuring that subsequent time-based operations (like light switching or move cooldowns) are correctly
     *       measured against this baseline.</li>
     *   <li><b>Resetting Game Variables:</b> Resets counters (e.g., finishedCount), flags (e.g., fieldCleared,
     *       gameOver, green), and prepares the game state for a new session. It calculates the initial duration until
     *       the next light switch by combining a minimum interval
     *       ({@code LIGHT_SWITCH_MINIMUM_INTERVAL_IN_MILLISECONDS}) with a random component governed by
     *       {@code LIGHT_SWITCH_TIME_VARIABILITY_IN_MILLISECONDS}.</li>
     *   <li><b>Light Switch Timer Setup:</b> Records the initial light switch time using the system's current nanotime
     *       to accurately track the time elapsed for light switching logic.</li>
     * </ul>
     * </p>
     */
    public void initGame()
    {
        // Create list of players and position them in a random place.
        players = new ArrayList<>();
        for(int i = 0; i < NUMBER_OF_PLAYERS; i++)
        {
            final int startX;
            final T p;

            startX = random.nextInt(GRID_WIDTH_IN_NUMBER_OF_CELLS);
            p = (T) new Player(startX, GRID_HEIGHT_IN_NUMBER_OF_CELLS - BOTTOM_OFFSET_IN_NUMBER_OF_CELLS);

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
     * Updates the game state based on the current time and processes multiple aspects of gameplay.
     * <p>
     * This method is the primary driver that progresses the game state. It handles:
     * <ul>
     *   <li><b>NPC Movement:</b>
     *       <ul>
     *         <li>During green light periods, non-user NPCs attempt to move with a probability defined by
     *             {@code NPC_MOVING_SPEED_PROBABILITY}, using fixed movement deltas
     *             ({@code NPC_MOVING_DISTANCE_ON_X_AXIS} and {@code NPC_MOVING_DISTANCE_ON_Y_AXIS}).</li>
     *         <li>During red light, NPCs may still move, but with a significantly lower chance determined by
     *             {@code NPC_MOVING_IN_RED_LIGHT_PROBABILITY}. The direction of movement is randomly selected
     *             from all available movement directions.</li>
     *       </ul>
     *   </li>
     *   <li><b>Red Light Enforcement:</b> If the light is red, the method checks if any player (other than those
     *       already marked as finished) has moved from their previous position. Any such movement results in immediate
     *       elimination (with sound effects played), potentially marking the game as over if the eliminated player
     *       is the user.</li>
     *   <li><b>Finish Line Processing:</b>
     *       <ul>
     *         <li>Players are checked against a finish line condition (using
     *         {@code FINISH_LINE_Y_IN_NUMBER_OF_CELLS}).</li>
     *         <li>If a player reaches the finish line and the count of finished players is below the limit defined by
     *             {@code ALLOWED_NUMBER_OF_WINNERS}, the player is marked as finished and the finished count is
     *             incremented.</li>
     *         <li>If the finished count exceeds this limit, the player is eliminated, triggering a death sequence.</li>
     *       </ul>
     *   </li>
     *   <li><b>Victory and Game Over Conditions:</b> Specifically checks whether the user has reached the finish line
     *      (with their y-coordinate less than or equal to the designated finish line) and is not eliminated, marking
     *      a win and ending the game.</li>
     *   <li><b>Light Timer Update:</b> Calculates the elapsed time (in milliseconds) since the last light switch. If
     *       the elapsed time exceeds the predetermined interval ({@code nextSwitchInMilliseconds}), the light color
     *       toggles between green and red. The timer is then reset with a new interval, which depends on whether the
     *       light is currently green or red (using appropriate minimum intervals and variability).</li>
     *   <li><b>Position Tracking:</b> After all game actions are processed, each player's previous position is updated
     *       to reflect their current position. This mechanism is essential for detecting subsequent movement changes
     *       in the next update cycle.</li>
     * </ul>
     * </p>
     *
     * @param now the current system time in nanoseconds used to drive time-dependent game logic.
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
     * Attempts to move the given player by the specified delta (dx, dy), applying push logic if the target cell is
     * occupied.
     * <p>
     * The method distinguishes between voluntary moves (initiated by the player) and forced moves (resulting from
     * cascading pushes):
     * <ul>
     *   <li><b>Voluntary Moves:</b> If the move is initiated voluntarily (when {@code initiating} is {@code true})
     *       and the target cell is occupied, the player does not move into the cell immediately; instead, it attempts
     *       to push the occupying player. If the user is initiating the move, a sound effect for pushing is triggered,
     *       and visual indicators (flags) are temporarily set for both the pusher and the pushed player to enable
     *       animation effects.</li>
     *   <li><b>Forced Moves:</b> When the move is not voluntary (i.e., as part of a push sequence with
     *       {@code initiating} equal to {@code false}), the player is allowed to move into the target cell
     *       unconditionally after a successful recursive push of any occupant occupying that cell.</li>
     * </ul>
     * Additionally, the method enforces the following constraints:
     * <ul>
     *   <li><b>Move Cooldown:</b> The player can move only if the time elapsed since their last move exceeds a
     *       cooldown threshold defined by {@code MOVE_COOLDOWN_IN_NANOSECONDS}. This prevents excessively frequent
     *       moves.</li>
     *   <li><b>Grid Boundaries:</b> The method ensures that the new position (after applying dx and dy) is within
     *       the valid grid bounds, as defined by the game’s constants. If the move would result in a position outside
     *       these bounds, the move is rejected.</li>
     *   <li><b>Cyclic Push Prevention:</b> To avoid infinite recursion or cyclic pushing scenarios, a list of players
     *       that have already been processed ({@code visited}) is maintained. If the current player is already present
     *       in this list, the push attempt is aborted.</li>
     * </ul>
     * </p>
     *
     * @param p           the player to attempt moving.
     * @param dx          the change in the x-coordinate.
     * @param dy          the change in the y-coordinate.
     * @param visited     a list of players already involved in the current push chain, used to prevent cyclic behavior.
     * @param initiating  {@code true} if this move is initiated voluntarily by the player; {@code false} if it is forced as part of a push sequence.
     * @return {@code true} if the move (or resulting push) was successfully executed; {@code false} if the move could not be completed.
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
