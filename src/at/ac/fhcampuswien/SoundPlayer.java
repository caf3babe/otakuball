package at.ac.fhcampuswien;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SoundPlayer {

    private Clip clip;
    private double clipDurationInSeconds;

    public SoundPlayer(String soundFile) {

        //read the audio file from the source
        InputStream src = this.getClass().getClassLoader().getResourceAsStream(soundFile);
        //add a buffer for mark/reset support
        InputStream bufferedSrc = new BufferedInputStream(src);

        AudioInputStream ais = null;
        try {
            ais = AudioSystem.getAudioInputStream(bufferedSrc);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        clip = null;
        try {
            clip = AudioSystem.getClip();
        } catch (
                LineUnavailableException e) {
            e.printStackTrace();
        }
        try {
            clip.open(ais);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clipDurationInSeconds = (ais.getFrameLength()+0.0)/ais.getFormat().getFrameRate();
    }

    public void startPlaying(){
        clip.start();
    }

    public void stopPlaying(){
        clip.stop();
    }

    public double getClipDurationInSeconds(){
        return clipDurationInSeconds;
    }

}

