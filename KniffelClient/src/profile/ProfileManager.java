package profile;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageFilter;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import application.Main;
import client.Client;
import javafx.scene.image.Image;

public class ProfileManager {
	
	
	private static ProfileManager instance;
	
	/**
	 * private constructor, so no class can instanciate from ProfileManager
	 */
	private ProfileManager() {
		
	}
	
	/**
	 * uses Singleton design pattern
	 * @return one and only instance of ProfileManager
	 */
	public static ProfileManager getInstance() {
		if(instance == null) {
			instance = new ProfileManager();
		}
		return instance;
	}

	/**
	 * 
	 * @return the selected profile picture of the client
	 */
	public Image getProfileImage() {
		Image img = new Image("profile/p"+Client.getInstance().getProfilePicID()+".png");
		return (img == null) ? new Image("profile/p0.png") : img;
	}
	
	/**
	 * 
	 * @param id of the profile picture we want to return
	 * @return profile picture with id
	 */
	public Image getImage(int id) {
		return new Image("profile/p"+id+".png");
	}
	
}
