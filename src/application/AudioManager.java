package application;

import java.io.File;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioManager
{
	private static AudioClip[] shootClips;
	private static int lastShootClipIndex;

	private static AudioClip[] explodeClips;
	private static int lastExplodeClipIndex;
	
	private static MediaPlayer bgmPlayer;

	public static void init()
	{
		shootClips = new AudioClip[25];
		for (int i = 0; i < shootClips.length; i++)
		{
			shootClips[i] = new AudioClip("file:assets/audio/shot0.wav");
		}

		explodeClips = new AudioClip[25];
		for (int i = 0; i < explodeClips.length; i++)
		{
			explodeClips[i] = new AudioClip("file:assets/audio/explode" + Integer.toString((int)(Math.random() * 3 + 1)) + ".wav");
		}

		// Only start playing music on first time
		if (bgmPlayer == null)
		{
			File backFile = new File ("assets/audio/bg.mp3");
			Media bgm = new Media(backFile.toURI().toString());
			bgmPlayer = new MediaPlayer(bgm);
			bgmPlayer.setOnEndOfMedia(new Runnable() {
			public void run()
			{
				bgmPlayer.seek(Duration.ZERO);
			}
			});
			bgmPlayer.play();
		}
	}
	
	// Method to play shoot clip
	public static void playShoot()
	{
		shootClips[lastShootClipIndex].play(0.5);

		// Loop index
		lastShootClipIndex++;
		if (lastShootClipIndex == shootClips.length)
		{
			lastShootClipIndex = 0;
		}
	}

	// Method to play explosion clip
	public static void playExplosion()
	{
		explodeClips[lastExplodeClipIndex].play(0.7);

		// Loop index
		lastExplodeClipIndex++;
		if (lastExplodeClipIndex == explodeClips.length)
		{
			lastExplodeClipIndex = 0;
		}
	}
}
