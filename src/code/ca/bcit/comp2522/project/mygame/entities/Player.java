package ca.bcit.comp2522.project.mygame.entities;

/**
 * The {@code Player} class represents an individual participant within the game,
 * whether controlled by the user or managed by game logic for non-player characters (NPCs).
 * <p>
 * Each player maintains its current position and a record of its previous position,
 * which is critical for determining movement deltas and validating moves. In addition,
 * the class tracks several state flags that indicate if the player is controlled by the user,
 * whether the player has been eliminated, whether it has successfully finished the game,
 * and flags used during push interactions (i.e., if the player is pushing or being pushed).
 * The class also records timestamps (in nanoseconds) for when the player last moved as well as
 * when it was eliminated, aiding in enforcing cooldowns and determining the order of events.
 * </p>
 * <p>
 * This class is designed for grid-based gameplay environments where valid coordinates must be non-negative.
 * To ensure robustness, the constructor validates that the provided initial coordinates meet this requirement.
 * Once constructed, a {@code Player} is initialized with the following default state:
 * <ul>
 *   <li>The current and previous coordinates are set to the provided starting values.</li>
 *   <li>The player is not flagged as a user-controlled player.</li>
 *   <li>The player is not eliminated and is considered active in the game.</li>
 *   <li>No movement or push interactions have yet taken place.</li>
 * </ul>
 * </p>
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class Player
{
    private int x;
    private int y;
    private int prevX;
    private int prevY;
    private boolean user;
    private boolean eliminated;
    private boolean finished; // This indicates whether the player finished the game by reaching finish line.
    private boolean pushing = false;  // This indicates that this player is currently pushing.
    private boolean pushed = false;   // This indicates that this player is currently being pushed down.
    private long deathTimestampInNanoseconds = 0;
    private long lastMoveTimestampInNanoseconds = 0;

    /**
     * Constructs a new {@code Player} with the specified starting coordinates.
     * <p>
     * The coordinates ({@code x}, {@code y}) represent the initial position of the player within the game grid.
     * This constructor validates that both coordinates are non-negative by invoking an internal helper method.
     * Upon successful validation, the player's current and previous positions are initialized to these values.
     * Moreover, the player's control and status flags (including {@code isUser}, {@code isEliminated}, and
     * {@code finished}) are initialized to {@code false}, indicating that the player is active and has not yet been
     * assigned a specialized role.
     * </p>
     *
     * @param x the starting x-coordinate of the player; must be a non-negative integer.
     * @param y the starting y-coordinate of the player; must be a non-negative integer.
     * @throws IllegalArgumentException if either {@code x} or {@code y} is negative.
     */
    public Player(final int x,
                  final int y)
    {
        validateCoordinates(x, y);
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
        this.user = false;
        this.eliminated = false;
        this.finished = false;
    }

    /**
     * Returns the current x-coordinate of the player.
     *
     * @return the current x-coordinate.
     */
    public int getX()
    {
        return x;
    }

    /**
     * Returns the current y-coordinate of the player.
     *
     * @return the current y-coordinate.
     */
    public int getY()
    {
        return y;
    }

    /**
     * Returns the previous x-coordinate of the player.
     *
     * @return the previous x-coordinate.
     */
    public int getPrevX()
    {
        return prevX;
    }

    /**
     * Returns the previous y-coordinate of the player.
     *
     * @return the previous y-coordinate.
     */
    public int getPrevY()
    {
        return prevY;
    }

    /**
     * Indicates whether the player is currently pushing.
     *
     * @return {@code true} if the player is pushing; {@code false} otherwise.
     */
    public boolean isPushing()
    {
        return pushing;
    }

    /**
     * Indicates whether the player is currently being pushed.
     *
     * @return {@code true} if the player is being pushed; {@code false} otherwise.
     */
    public boolean isPushed()
    {
        return pushed;
    }

    /**
     * Returns the timestamp (in nanoseconds) at which the player was eliminated.
     *
     * @return the death timestamp.
     */
    public long getDeathTimestampInNanoseconds()
    {
        return deathTimestampInNanoseconds;
    }

    /**
     * Returns the last move time (in nanoseconds) of the player.
     *
     * @return the last move time.
     */
    public long getLastMoveTimestampInNanoseconds()
    {
        return lastMoveTimestampInNanoseconds;
    }

    /**
     * Indicates whether this player is controlled by the user.
     *
     * @return {@code true} if the player is the user; {@code false} otherwise.
     */
    public boolean isUser()
    {
        return user;
    }

    /**
     * Indicates whether the player has been eliminated.
     *
     * @return {@code true} if the player is eliminated; {@code false} otherwise.
     */
    public boolean isEliminated()
    {
        return eliminated;
    }

    /**
     * Indicates whether the player has finished the game.
     *
     * @return {@code true} if the player has finished; {@code false} otherwise.
     */
    public boolean isFinished()
    {
        return finished;
    }

    /**
     * Sets the current x-coordinate of the player.
     *
     * @param x the new x-coordinate.
     */
    public void setX(final int x)
    {
        this.x = x;
    }

    /**
     * Sets the current y-coordinate of the player.
     *
     * @param y the new y-coordinate.
     */
    public void setY(final int y)
    {
        this.y = y;
    }

    /**
     * Sets the previous x-coordinate of the player.
     *
     * @param prevX the new previous x-coordinate.
     */
    public void setPrevX(final int prevX)
    {
        this.prevX = prevX;
    }

    /**
     * Sets the previous y-coordinate of the player.
     *
     * @param prevY the new previous y-coordinate.
     */
    public void setPrevY(final int prevY)
    {
        this.prevY = prevY;
    }

    /**
     * Sets whether the player is currently pushing.
     *
     * @param pushing {@code true} if the player is pushing; {@code false} otherwise.
     */
    public void setPushing(final boolean pushing)
    {
        this.pushing = pushing;
    }

    /**
     * Sets whether the player is currently being pushed.
     *
     * @param pushed {@code true} if the player is being pushed; {@code false} otherwise.
     */
    public void setPushed(final boolean pushed)
    {
        this.pushed = pushed;
    }

    /**
     * Sets the timestamp at which the player was eliminated.
     *
     * @param deathTimestamp the death timestamp in nanoseconds.
     */
    public void setDeathTimeStamp(final long deathTimestamp)
    {
        this.deathTimestampInNanoseconds = deathTimestamp;
    }

    /**
     * Sets the last move time for the player.
     *
     * @param lastMoveTime the new last move time in nanoseconds.
     */
    public void setLastMoveTimestampInNanoseconds(final long lastMoveTime)
    {
        this.lastMoveTimestampInNanoseconds = lastMoveTime;
    }

    /**
     * Sets whether this player is controlled by the user.
     *
     * @param user {@code true} if the player is the user; {@code false} otherwise.
     */
    public void setUser(final boolean user)
    {
        this.user = user;
    }

    /**
     * Sets the eliminated status of the player.
     *
     * @param eliminated {@code true} if the player should be marked as eliminated; {@code false} otherwise.
     */
    public void setEliminated(final boolean eliminated)
    {
        this.eliminated = eliminated;
    }

    /**
     * Sets the finished status of the player.
     *
     * @param finished {@code true} if the player has finished the game; {@code false} otherwise.
     */
    public void setFinished(final boolean finished)
    {
        this.finished = finished;
    }

    /*
     * Validates that the provided coordinates are non-negative.
     *
     * @param x the x-coordinate to validate
     * @param y the y-coordinate to validate
     * @throws IllegalArgumentException if either coordinate is negative
     */
    private static void validateCoordinates(final int x,
                                            final int y)
    {
        if(x < 0 || y < 0)
        {
            throw new IllegalArgumentException("Coordinates must be non-negative. Provided x: " + x + ", y: " + y);
        }
    }
}
