package ca.bcit.comp2522.project.numbergame;

import java.util.concurrent.CountDownLatch;

/**
 * Represents a game that can be played.
 * <p>
 * Implementations of this interface must define the game logic within the
 * {@link #play(CountDownLatch)} method. When the game is complete, the implementation
 * should signal its completion by calling {@code latch.countDown()}.
 * </p>
 */
public interface Game
{
    /**
     * Starts the game. When the game is over,
     * the game should call latch.countDown().
     *
     * @param latch a CountDownLatch to signal game completion
     */
    void play(CountDownLatch latch);
}