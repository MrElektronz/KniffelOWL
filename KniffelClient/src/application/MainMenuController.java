package application;

import java.io.IOException;

import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import profile.ProfileManager;

public class MainMenuController {
	

	@FXML
	private Label username;
	
	@FXML
	private ImageView imageProfile;
	
	public void initialize() {
		username.setText(Client.getInstance().getUsername());
		updateProfileImage();
	}
	
	/**
	 * handles the process after the 'Play vs Bots'-Button has been clicked
	 * @param event
	 * @throws IOException
	 */
	public void onPlayBotEasy(ActionEvent event) throws IOException {
		Main.changeScene("Scene_GameBoard.fxml",1280,745);
	}
	
	public void onPlayBotMedium(ActionEvent event) throws IOException {
		Main.changeScene("Scene_GameBoard.fxml",1280,745);
	}
	
	public void onPlayBotHard(ActionEvent event) throws IOException {
		Main.changeScene("Scene_GameBoard.fxml",1280,745);
	}
	
	public void onPlayOnline(ActionEvent event) throws IOException {
		Main.changeScene("Scene_Lobby.fxml",640,440);
	}
	
	public void onImageChanged(ActionEvent event) throws IOException{
		Parent root = FXMLLoader.load(Main.class.getResource("Scene_ChangeProfileImage.fxml"));
		Main.mainMenu = this;
		Scene scene = new Scene(root,312,85);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
	}
	
	public void updateProfileImage() {
		Image img = ProfileManager.getInstance().getProfileImage();
		imageProfile.setImage(img);
	}
}
