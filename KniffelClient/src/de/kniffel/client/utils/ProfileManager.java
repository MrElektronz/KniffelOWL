package de.kniffel.client.utils;

import javafx.scene.image.Image;

/**
 * A helper class to access the profile pictures
 * 
 * @author KBeck
 */
public class ProfileManager {

	private static ProfileManager instance;

	/**
	 * private constructor, so no class can instanciate from ProfileManager
	 */
	private ProfileManager() {
	}

	/**
	 * uses Singleton design pattern
	 * 
	 * @return one and only instance of ProfileManager
	 */
	public static ProfileManager getInstance() {
		if (instance == null) {
			instance = new ProfileManager();
		}
		return instance;
	}

	/**
	 * 
	 * @return the selected profile picture of the client
	 */
	public Image getProfileImage() {
		Image img = new Image("de/kniffel/client/utils/p" + Client.getInstance().getProfilePicID() + ".png");
		return (img == null) ? new Image("de/kniffel/client/utils/p0.png") : img;
	}

	/**
	 * 
	 * @param id of the profile picture we want to return
	 * @return profile picture with id
	 */
	public Image getImage(int id) {
		return new Image("de/kniffel/client/utils/p" + id + ".png");
	}

}
