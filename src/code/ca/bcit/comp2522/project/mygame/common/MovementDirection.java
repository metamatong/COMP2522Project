package ca.bcit.comp2522.project.mygame.common;

/**
 * Enumerates the possible directions of movement for entities within the game.
 * <p>
 * This enumeration defines the four primary cardinal directions: UP, DOWN, LEFT, and RIGHT.
 * These constants are used throughout the game to standardize movement logic and determine
 * the direction in which a game entity (such as a player or NPC) is intended to move.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 *     MovementDirection direction = MovementDirection.UP;
 *     if (direction == MovementDirection.UP) {
 *         // Execute logic to move the entity upward
 *     }
 * </pre>
 * </p>
 * <p>
 * <b>Details:</b>
 * <ul>
 *     <li>{@code UP}: Represents movement toward the top of the screen.</li>
 *     <li>{@code DOWN}: Represents movement toward the bottom of the screen.</li>
 *     <li>{@code LEFT}: Represents movement toward the left-hand side of the screen.</li>
 *     <li>{@code RIGHT}: Represents movement toward the right-hand side of the screen.</li>
 * </ul>
 * </p>
 *
 * @see ca.bcit.comp2522.project.mygame.common.GameConfig
 * @author Kyle Cheon
 * @version 1.0
 */
public enum MovementDirection
{

    /**
     * Represents upward movement, typically toward the top of the display area.
     */
    UP,

    /**
     * Represents downward movement, typically toward the bottom of the display area.
     */
    DOWN,

    /**
     * Represents leftward movement, typically toward the left-hand side of the display area.
     */
    LEFT,

    /**
     * Represents rightward movement, typically toward the right-hand side of the display area.
     */
    RIGHT
}
