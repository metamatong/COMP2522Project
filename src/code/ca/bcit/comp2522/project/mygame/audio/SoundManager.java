package ca.bcit.comp2522.project.mygame.audio;

import javafx.scene.media.AudioClip;

/**
 * Manages the playback of various sound effects and background music for the game.
 * <p>
 * This class is responsible for loading and playing audio clips from the application's
 * resources. It handles individual sound effects such as push, gunshot, and death sounds,
 * as well as the control of looping background music (BGM). The background music is designed
 * to loop indefinitely until explicitly stopped. This class uses JavaFX's {@link AudioClip}
 * for efficient audio playback.
 * </p>
 * <p>
 * Audio resources used:
 * <ul>
 *   <li>{@code /push.wav}: Sound effect for a push action.</li>
 *   <li>{@code /gunshot.mp3}: Sound effect for a gunshot.</li>
 *   <li>{@code /deathone.mp3}: First variant for a death sound effect.</li>
 *   <li>{@code /deathtwo.mp3}: Second variant for a death sound effect.</li>
 *   <li>{@code /bgm.mp3}: Background music track, intended to loop during gameplay.</li>
 * </ul>
 * The background music was sourced from an external provider
 * (see the inline reference in the constructor for details).
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
     * Constructs a new {@code SoundManager} and loads all required audio clips from
     * the resource files.
     * <p>
     * The audio clips are loaded using {@code getClass().getResource()} to locate the audio
     * files bundled with the application. The files and their corresponding uses are:
     * <ul>
     *   <li>{@code "/push.wav"} for the push sound effect</li>
     *   <li>{@code "/gunshot.mp3"} for the gunshot sound effect</li>
     *   <li>{@code "/deathone.mp3"} for the first death sound effect</li>
     *   <li>{@code "/deathtwo.mp3"} for the second death sound effect</li>
     *   <li>{@code "/bgm.mp3"} for the background music. The background music used in this project
     *       is referenced in the code (originally sourced from an external URL on SoundCloud).</li>
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
     * Plays the background music (BGM) in a continuous loop if it is not already playing.
     * <p>
     * This method starts the background music and sets it to loop indefinitely. The music
     * will continue to play until {@link #stopBGM()} is invoked. If the background music is
     * already playing, this method will have no effect to prevent overlapping audio.
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
     * <p>
     * This method halts the playback of the background music and resets the playing flag,
     * thereby allowing a subsequent invocation of {@link #playBGM()} to restart the music.
     * </p>
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
