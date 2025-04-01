package ca.bcit.comp2522.project.mygame.entities;

/**
 * Represents a player entity in the game.
 * <p>
 * This class maintains the player's current and previous positions, status flags, and timestamps for movement
 * and elimination. It provides getter and setter methods to access and modify the player's state, including whether
 * the player is controlled by the user, eliminated, has finished, is pushing, or is being pushed.
 * </p>
 * @author Kyle Cheon
 * @version 1.0
 */
public class Player
{
    private int x;
    private int y;
    private int prevX;
    private int prevY;
    private boolean isUser;
    private boolean isEliminated;
    private boolean finished; // This indicates whether the player finished the game by reaching finish line.
    private boolean pushing = false;  // This indicates that this player is currently pushing.
    private boolean pushed = false;   // This indicates that this player is currently being pushed down.
    private long deathTimestampInNanoseconds = 0;
    private long lastMoveTimestampInNanoseconds = 0;

    /**
     * Constructs a new player with the specified starting position.
     *
     * @param x the starting x-coordinate of the player.
     * @param y the starting y-coordinate of the player.
     */
    public Player(final int x,
                  final int y)
    {
        validateCoordinates(x, y);
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
        this.isUser = false;
        this.isEliminated = false;
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
        return isUser;
    }

    /**
     * Indicates whether the player has been eliminated.
     *
     * @return {@code true} if the player is eliminated; {@code false} otherwise.
     */
    public boolean isEliminated()
    {
        return isEliminated;
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
     * @param isPushing {@code true} if the player is pushing; {@code false} otherwise.
     */
    public void setPushing(final boolean isPushing)
    {
        this.pushing = isPushing;
    }

    /**
     * Sets whether the player is currently being pushed.
     *
     * @param isPushed {@code true} if the player is being pushed; {@code false} otherwise.
     */
    public void setPushed(final boolean isPushed)
    {
        this.pushed = isPushed;
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
     * @param isUser {@code true} if the player is the user; {@code false} otherwise.
     */
    public void setUser(final boolean isUser)
    {
        this.isUser = isUser;
    }

    /**
     * Sets the eliminated status of the player.
     *
     * @param isEliminated {@code true} if the player should be marked as eliminated; {@code false} otherwise.
     */
    public void setEliminated(final boolean isEliminated)
    {
        this.isEliminated = isEliminated;
    }

    /**
     * Sets the finished status of the player.
     *
     * @param isFinished {@code true} if the player has finished the game; {@code false} otherwise.
     */
    public void setFinished(final boolean isFinished)
    {
        this.finished = isFinished;
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
