package ca.bcit.comp2522.project.mygame.common;

/**
 * Represents the different states that the game can be in.
 * <p>
 * The game state can be one of the following:
 * <ul>
 *   <li>{@code INTRO} - The game is in the introduction or start screen.</li>
 *   <li>{@code GAME} - The game is actively running.</li>
 *   <li>{@code GAME_OVER} - The game has ended, typically due to player failure.</li>
 * </ul>
 * </p>
 * @author Kyle Cheon
 * @version 1.0
 */
public enum GameState
{
    /**
     * Indicates that the introductory screen is shown.
     */
    INTRO,

    /**
     * Indicates that the main part of the game is being played.
     */
    GAME,

    /**
     * Indicates that the game has ended.
     */
    GAME_OVER
}
