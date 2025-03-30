package ca.bcit.comp2522.project.mygame.audio;

import javafx.scene.media.AudioClip;

public class SoundManager
{
    private AudioClip pushSound;
    private AudioClip gunshotSound;
    private AudioClip deathSound1;
    private AudioClip deathSound2;
    private AudioClip bgm;
    private boolean bgmPlaying = false;

    public SoundManager() {
        pushSound = new AudioClip(getClass().getResource("/push.wav").toExternalForm());
        gunshotSound = new AudioClip(getClass().getResource("/gunshot.mp3").toExternalForm());
        deathSound1 = new AudioClip(getClass().getResource("/deathone.mp3").toExternalForm());
        deathSound2 = new AudioClip(getClass().getResource("/deathtwo.mp3").toExternalForm());
        bgm = new AudioClip(getClass().getResource("/bgm.mp3").toExternalForm()); //https://soundcloud.com/extiox/squid-game-red-light-green-light
    }

    public boolean isBgmPlaying() {
        return bgmPlaying;
    }

    public void playPushSound() {
        pushSound.play();
    }

    public void playGunshotSound() {
        gunshotSound.play();
    }

    public void playDeathSound1() {
        deathSound1.play();
    }

    public void playDeathSound2() {
        deathSound2.play();
    }

    public void playBGM() {
        if (!bgmPlaying) {
            bgm.setCycleCount(AudioClip.INDEFINITE);
            bgm.play();
            bgmPlaying = true;
        }
    }

    public void stopBGM() {
        if (bgmPlaying) {
            bgm.stop();
            bgmPlaying = false;
        }
    }
}
