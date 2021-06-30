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

	public static Image getProfileImage() {
		Image img = new Image("profile/p"+Client.getProfilePicID()+".png");
		return (img == null) ? new Image("profile/p0.png") : img;
	}
	
	public static Image getImage(int id) {
		return new Image("profile/p"+id+".png");
	}
	
}
