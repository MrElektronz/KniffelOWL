package de.kniffel.client.controllers;

import java.io.IOException;

import de.kniffel.client.main.ClientMain;
import de.kniffel.client.utils.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller class of the LogIn-Scene. Sets username and password and displays
 * the possibilities to change to "Register" and "ResetPW"-Scene
 * 
 * @author KBeck
 * 
 */
public class LogInController {

	@FXML
	private Button button;
	@FXML
	private Label errorLabel;
	@FXML
	private TextField username;
	@FXML
	private PasswordField password;
	@FXML
	private CheckBox keepSignedIn;

	private Client client;

	/**
	 * gets called on startup, automatically enters username and password if
	 * possible
	 */
	public void initialize() {
		client = Client.getInstance();

		// Fills the username and password Textfields
		this.username.setText(ClientMain.readProperty("client.user"));
		this.password.setText(ClientMain.readProperty("client.password"));
	}

	/**
	 * Change the scene to register a new user
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onRegisterHere(ActionEvent event) throws IOException {
		ClientMain.changeScene("Scene_Register.fxml");
	}

	/**
	 * Change the scene to reset the password of a knows user
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onForgotPassword(ActionEvent event) throws IOException {
		ClientMain.changeScene("Scene_ResetPW.fxml");
	}

	/**
	 * handles the process after the 'Login'-Button has been clicked
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onLogin(ActionEvent event) throws IOException {
		if (username.getText() != null && password.getText() != null) {

			if (keepSignedIn.isSelected()) {
				// Store username and password in config file
				ClientMain.updateUserPassword(username.getText(), password.getText());
			}

			// tries to log in and stores the answer of this request in "sessionID"
			String sessionID = client.login(username.getText(), password.getText());

			if (sessionID == null) {
				errorLabel.setText("Ein Fehler ist aufgetreten");
			} else if (sessionID.equals("0")) {
				errorLabel.setText("Der Nutzername/Password ist falsch");
			} else if (sessionID.equals("-1")) {
				errorLabel.setText("Dieser Nutzer ist bereits eingeloggt");
			} else {
				errorLabel.setText("Du wurdest erfolgreich eingeloggt");
				client.updateProfilePic();
				ClientMain.changeScene("Scene_MainMenu.fxml", 1280, 720);
			}
		}

	}
}
