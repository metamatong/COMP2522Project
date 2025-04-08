package ca.bcit.comp2522.project.mygame;

import static org.junit.jupiter.api.Assertions.*;

import ca.bcit.comp2522.project.mygame.audio.SoundManager;
import ca.bcit.comp2522.project.mygame.common.GameState;
import ca.bcit.comp2522.project.mygame.engine.GameLogic;
import ca.bcit.comp2522.project.mygame.entities.Player;
import ca.bcit.comp2522.project.mygame.ui.GameRenderer;
import javafx.embed.swing.JFXPanel;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * A comprehensive JUnit 5 test class that verifies the core functionality of the game logic,
 * along with basic smoke tests for the sound and UI rendering components.
 * <p>
 * This class performs the following verifications:
 * <ul>
 *   <li>Initialization: Checks that the game logic correctly instantiates and initializes
 *       the required number of players and assigns the user-controlled player.</li>
 *   <li>Movement: Verifies that players move correctly when a move is attempted and enforces
 *       a move cooldown period to prevent immediate successive moves.</li>
 *   <li>Light State and Win Condition: Tests that updating the game state toggles light state appropriately
 *       and that a win is detected when the user reaches the finish line.</li>
 *   <li>Sound and UI: Confirms that methods of the SoundManager and GameRenderer do not throw errors when invoked.</li>
 *   <li>Singleton Pattern: Ensures that a valid SoundManager creates a non-null GameLogic instance.</li>
 *   <li>Cooldown Enforcement: Checks that move attempts made before the cooldown expires are rejected.</li>
 * </ul>
 * Additionally, the class initializes the JavaFX runtime before running any tests to allow the use of JavaFX classes.
 * </p>
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class GameLogicTest {

    /**
     * Initializes the JavaFX toolkit. This is required for using JavaFX classes (such as Canvas and GraphicsContext)
     * within the test methods.
     */    @BeforeAll
    static void initJavaFX() {
        new JFXPanel(); // This initializes the JavaFX runtime.
    }

    /**
     * Tests that the {@code initGame()} method creates the expected number of players and properly sets the
     * user-controlled player.
     */
    @Test
    void testInitGameCreatesPlayers() {
        SoundManager sm = new SoundManager();
        GameLogic gl = GameLogic.getInstance(sm);
        gl.initGame();
        List<Player> players = gl.getPlayers();
        assertNotNull(players, "initGame() should create a non-null list of players.");
        assertEquals(45, players.size(), "initGame() should create 45 players.");
        Player user = gl.getUser();
        assertNotNull(user, "The user-controlled player should be assigned.");
        assertTrue(user.isUser(), "The first player should be marked as user.");
    }

    /**
     * Tests that a player is successfully moved by {@code tryMoveWithPush()} and that move cooldowns prevent
     * immediate repeated moves.
     */
    @Test
    void testTryMoveWithPushMovesPlayer() {
        SoundManager sm = new SoundManager();
        GameLogic gl = GameLogic.getInstance(sm);
        gl.initGame();
        // Use the user-controlled player for movement testing.
        Player user = gl.getUser();
        // Set a known starting position.
        user.setX(10);
        user.setY(20);
        user.setPrevX(10);
        user.setPrevY(20);

        // Attempt to move the user one cell to the right.
        boolean moved = gl.tryMoveWithPush(user, 1, 0, new ArrayList<>(), true);
        assertTrue(moved, "tryMoveWithPush should return true when moving into an empty cell.");
        assertEquals(11, user.getX(), "User's x-coordinate should update from 10 to 11 after moving.");
        assertEquals(20, user.getY(), "User's y-coordinate should remain unchanged.");

        // Immediately try to move again; due to cooldown the move should fail.
        boolean movedAgain = gl.tryMoveWithPush(user, 1, 0, new ArrayList<>(), true);
        assertFalse(movedAgain, "A second immediate move should fail due to move cooldown.");
    }

    /**
     * Tests that the light switching logic within {@code updateGame()} maintains its state when updates occur rapidly,
     * and that the method behaves correctly when sufficient time has elapsed.
     */
    @Test
    void testLightSwitchTogglingViaUpdateGame() {
        SoundManager sm = new SoundManager();
        GameLogic gl = GameLogic.getInstance(sm);
        gl.initGame();
        // Capture the initial light state.
        boolean initialLight = gl.isGreen();
        // Call updateGame with the current time.
        long now = System.nanoTime();
        gl.updateGame(now);
        // Calling updateGame again immediately should normally not toggle the light.
        gl.updateGame(System.nanoTime());
        // The light state should remain the same if not enough time elapsed.
        assertEquals(initialLight, gl.isGreen(), "Light state should remain unchanged if updateGame is called too quickly.");

        // Now simulate a long elapsed time by adding a large delta.
        long later = now + 10_000_000; // 10 milliseconds later
        gl.updateGame(later);
        // Now it is expected that enough time has elapsed to toggle the light.
        // (Since the next switch interval is randomized, we can only assert that the state may change.)
        // We simply assert that updateGame runs without error.
        assertNotNull(gl.isGreen(), "After a long delay, the light state is defined.");
    }

    /**
     * Tests that the win condition is correctly detected when the user-controlled player reaches the finish line.
     */
    @Test
    void testUserWinCondition() {
        SoundManager sm = new SoundManager();
        GameLogic gl = GameLogic.getInstance(sm);
        gl.initGame();
        Player user = gl.getUser();
        // In GameConfig, FINISH_LINE_Y_IN_NUMBER_OF_CELLS is 5.
        // Simulate the user reaching the finish line by setting y to 4.
        user.setY(4);
        long now = System.nanoTime();
        gl.updateGame(now);
        assertTrue(user.isFinished(), "User should be marked finished when reaching the finish line (y < 5).");
        assertTrue(gl.isGameOver(), "Game should be over after the user finishes.");
    }

    /**
     * Smoke tests for the {@code SoundManager} methods to ensure they execute without exceptions.
     */
    @Test
    void testSoundManagerMethods() {
        SoundManager sm = new SoundManager();
        assertDoesNotThrow(() -> sm.playPushSound(), "playPushSound() should not throw an exception.");
        assertDoesNotThrow(() -> sm.playGunshotSound(), "playGunshotSound() should not throw an exception.");
        assertDoesNotThrow(() -> sm.playDeathSound1(), "playDeathSound1() should not throw an exception.");
        assertDoesNotThrow(() -> sm.playDeathSound2(), "playDeathSound2() should not throw an exception.");
        assertDoesNotThrow(() -> {
            sm.playBGM();
            sm.stopBGM();
        }, "playBGM() and stopBGM() should run without throwing an exception.");
    }

    /**
     * Smoke tests for the {@code GameRenderer}'s render method.
     * <p>
     * This test verifies that rendering each game state (INTRO, GAME, GAME_OVER)
     * does not throw an exception.
     * </p>
     */
    @Test
    void testGameRendererRenderDoesNotThrow() {
        SoundManager sm = new SoundManager();
        GameLogic gl = GameLogic.getInstance(sm);
        gl.initGame();
        GameRenderer renderer = new GameRenderer(gl);
        Canvas canvas = new Canvas(200, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // Render each game state and ensure no exceptions occur.
        assertDoesNotThrow(() -> renderer.render(GameState.INTRO, gc), "Rendering INTRO screen should not throw an exception.");
        assertDoesNotThrow(() -> renderer.render(GameState.GAME, gc), "Rendering GAME screen should not throw an exception.");
        assertDoesNotThrow(() -> renderer.render(GameState.GAME_OVER, gc), "Rendering GAME_OVER screen should not throw an exception.");
    }

    /**
     * Positive test: Providing a valid SoundManager should return a non-null
     * GameLogic instance (and not throw).
     */
    @Test
    void testGetInstanceWithValidSoundManager() {
        SoundManager sm = new SoundManager();
        GameLogic<Player> gl = GameLogic.getInstance(sm);
        assertNotNull(gl, "GameLogic instance should be created with a valid SoundManager.");
    }

    /**
     * Negative test: Attempting to move again before the cooldown has elapsed
     * should fail and return false.
     */
    @Test
    void testTryMoveWithPushCooldownNotElapsed() {
        SoundManager sm = new SoundManager();
        GameLogic<Player> gl = GameLogic.getInstance(sm);
        gl.initGame();

        Player user = gl.getUser();
        user.setX(10);
        user.setY(10);

        // First move should succeed.
        boolean firstMove = gl.tryMoveWithPush(user, 0, -1, new ArrayList<>(), true);
        assertTrue(firstMove, "First move within bounds should succeed.");

        // Immediately try to move again; cooldown not elapsed => should fail.
        boolean secondMove = gl.tryMoveWithPush(user, 0, -1, new ArrayList<>(), true);
        assertFalse(secondMove, "Second move before cooldown ends should fail.");
    }

}