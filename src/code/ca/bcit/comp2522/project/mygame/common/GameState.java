package ca.bcit.comp2522.project.mygame.common;

/**
 * Enumerates the possible states of the game throughout its lifecycle.
 * <p>
 * This enumeration defines the different phases the game can be in. The states are used to control
 * and monitor the progress of the game, facilitating transitions between various stages such as the
 * introductory screen, active gameplay, and the game over sequence. Developers can leverage these
 * states to manage game logic, render appropriate screens, and enforce rules based on the current
 * state of the game.
 * </p>
 * <p>
 * The states defined are:
 * <ul>
 *   <li><b>INTRO</b> - Indicates that the game is in the introductory phase, where a start screen or
 *       welcome message is typically displayed before gameplay begins.</li>
 *   <li><b>GAME</b> - Indicates that the game is actively running, and the main gameplay loop is in progress.</li>
 *   <li><b>GAME_OVER</b> - Indicates that the game has ended, usually due to player failure or
 *       completion of the game, prompting end-of-game processing such as score display and cleanup.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Example usage in game logic:
 * <pre>
 *     if (gameState == GameState.INTRO) {
 *         // Display introduction screen
 *     } else if (gameState == GameState.GAME) {
 *         // Run main game loop
 *     } else if (gameState == GameState.GAME_OVER) {
 *         // Handle game over sequence
 *     }
 * </pre>
 * </p>
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public enum GameState {
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
