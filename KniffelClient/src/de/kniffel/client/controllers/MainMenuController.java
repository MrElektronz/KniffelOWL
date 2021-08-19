package de.kniffel.client.controllers;

import java.io.IOException;

import de.kniffel.client.gameplay.Bot.BotDifficulty;
import de.kniffel.client.main.ClientMain;
import de.kniffel.client.utils.Client;
import de.kniffel.client.utils.ProfileManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Controller class of the MainMenu-Scene. Displays buttons to play offline and
 * online, also gives the player the possibility to change the profile picture.
 * 
 * @author KBeck
 *
 */

public class MainMenuController {

	@FXML
	private Label username;

	@FXML
	private ImageView imageProfile;

	/**
	 * gets called whenever a scene with this controller assigned to it, starts
	 */
	public void initialize() {

		// display username and profile picture
		username.setText(Client.getInstance().getUsername());
		updateProfileImage();
	}

	/**
	 * handles the process after the 'Bot[Easy]'-Button has been clicked
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onPlayBotEasy(ActionEvent event) throws IOException {
		ClientMain.changeToOfflineScene(BotDifficulty.EASY);
	}

	/**
	 * 
	 * handles the process after the 'Bot[Medium]'-Button has been clicked
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onPlayBotMedium(ActionEvent event) throws IOException {
		ClientMain.changeToOfflineScene(BotDifficulty.MEDIUM);
	}

	/**
	 * handles the process after the 'Bot[Hard]'-Button has been clicked
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onPlayBotHard(ActionEvent event) throws IOException {
		ClientMain.changeToOfflineScene(BotDifficulty.HARD);
	}

	/**
	 * Starts joining a lobby to play online
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onPlayOnline(ActionEvent event) throws IOException {
		ClientMain.changeScene("Scene_Lobby.fxml", 640, 440);
	}

	/**
	 * Opens the scene to change the profile picture
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onImageChanged(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(ClientMain.class.getResource("Scene_ChangeProfileImage.fxml"));
		ClientMain.mainMenu = this;
		Scene scene = new Scene(root, 312, 85);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
	}

	/**
	 * Displays the profile picture
	 */
	public void updateProfileImage() {
		Image img = ProfileManager.getInstance().getProfileImage();
		imageProfile.setImage(img);
	}
}
