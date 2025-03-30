package ca.bcit.comp2522.project.mygame.audio;

import javafx.scene.media.AudioClip;

/**
 * Manages the playback of various sound effects and background music for the game.
 * <p>
 * This class loads audio clips from the application's resources and provides methods to play
 * individual sound effects such as push, gunshot, and death sounds, as well as control the looping
 * background music.
 * </p>
 *
 * @author Kyle Cheon
 * @version 1.0
 */
public class SoundManager
{
    private final AudioClip pushSound;
    private final AudioClip gunshotSound;
    private final AudioClip deathSound1;
    private final AudioClip deathSound2;
    private final AudioClip bgm;
    private boolean bgmPlaying = false;

    /**
     * Constructs a new SoundManager and loads all required audio clips from the resource files.
     * <p>
     * The audio clips are loaded from the following resources:
     * <ul>
     *   <li>"/push.wav" for the push sound effect</li>
     *   <li>"/gunshot.mp3" for the gunshot sound effect</li>
     *   <li>"/deathone.mp3" for the first death sound effect</li>
     *   <li>"/deathtwo.mp3" for the second death sound effect</li>
     *   <li>"/bgm.mp3" for the background music</li>
     * </ul>
     * </p>
     */
    public SoundManager() {
        pushSound = new AudioClip(getClass().getResource("/push.wav").toExternalForm());
        gunshotSound = new AudioClip(getClass().getResource("/gunshot.mp3").toExternalForm());
        deathSound1 = new AudioClip(getClass().getResource("/deathone.mp3").toExternalForm());
        deathSound2 = new AudioClip(getClass().getResource("/deathtwo.mp3").toExternalForm());
        // following bgm is from https://soundcloud.com/extiox/squid-game-red-light-green-light
        bgm = new AudioClip(getClass().getResource("/bgm.mp3").toExternalForm());
    }

    /**
     * Plays the push sound effect.
     */
    public void playPushSound()
    {
        pushSound.play();
    }

    /**
     * Plays the gunshot sound effect.
     */
    public void playGunshotSound()
    {
        gunshotSound.play();
    }

    /**
     * Plays the first death sound effect.
     */
    public void playDeathSound1()
    {
        deathSound1.play();
    }

    /**
     * Plays the second death sound effect.
     */
    public void playDeathSound2()
    {
        deathSound2.play();
    }

    /**
     * Plays the background music in a continuous loop if it is not already playing.
     * <p>
     * The background music is set to loop indefinitely until it is stopped.
     * </p>
     */
    public void playBGM()
    {
        if(!bgmPlaying)
        {
            bgm.setCycleCount(AudioClip.INDEFINITE);
            bgm.play();
            bgmPlaying = true;
        }
    }

    /**
     * Stops the background music if it is currently playing.
     */
    public void stopBGM()
    {
        if(bgmPlaying)
        {
            bgm.stop();
            bgmPlaying = false;
        }
    }
}
