package application;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class MainMenuController {
	

	

	
	/**
	 * handles the process after the 'Play vs Bots'-Button has been clicked
	 * @param event
	 * @throws IOException
	 */
	public void onPlayOffline(ActionEvent event) throws IOException {
		Main.changeScene("Scene_GameBoard.fxml",1280,745);
	}
}
