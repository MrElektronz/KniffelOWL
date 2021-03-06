package de.kniffel.client.sound;

import java.util.Random;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * A class which can be used to play rolling sounds. These sounds were recorded
 * by @KevinKloidt
 * 
 * @author KBeck
 */

public class SoundGenerator {

	private Media roll1;
	private Media roll2;
	private Random random;

	public SoundGenerator() {
		roll1 = new Media(SoundGenerator.class.getResource("Wuerfeln_1.m4a").toString());
		roll2 = new Media(SoundGenerator.class.getResource("Wuerfeln_2.m4a").toString());
		random = new Random();
	}

	/**
	 * Plays one random roll dice sound
	 */
	public void playRoll() {
		Media m = roll1;
		if (random.nextBoolean()) {
			m = roll2;
		}
		MediaPlayer mediaPlayer = new MediaPlayer(m);
		mediaPlayer.setVolume(0.15);
		mediaPlayer.play();
	}

}
