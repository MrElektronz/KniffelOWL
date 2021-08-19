package de.kniffel.client.controllers;

import java.io.IOException;

import de.kniffel.client.main.ClientMain;
import de.kniffel.client.utils.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * 
 * Controller class of the Register-Scene. Lets the user create a new account.
 * 
 * @author KBeck
 *
 */

public class RegisterController {

	@FXML
	private Button bSendCode;
	@FXML
	private Label errorLabel;
	@FXML
	private TextField username;
	@FXML
	private TextField email;
	@FXML
	private TextField emailCode;
	@FXML
	private PasswordField password;
	@FXML
	private PasswordField password2;
	@FXML
	private Label successLabel;

	public void onBackToLogin(ActionEvent event) throws IOException {
		ClientMain.changeScene("Scene_Login.fxml");
	}

	public void onSendCode(ActionEvent event) throws IOException {
		errorLabel.setText("You can simply register without receiving a code");
	}

	/**
	 * handles the process after the 'Login'-Button has been clicked
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onRegister(ActionEvent event) throws IOException {
		successLabel.setVisible(false);
		if (username.getText().length() > 1 && password.getText().length() > 1 && password2.getText().length() > 1
				&& email.getText().length() > 1) {
			if (password.getText().equals(password2.getText())) {
				int answer = Client.getInstance().register(username.getText(), password.getText(), email.getText());
				errorLabel.setVisible(true);
				successLabel.setVisible(false);
				successLabel.setText("");
				if (answer == 1) {
					successLabel.setVisible(true);
					errorLabel.setVisible(false);
					successLabel.setText("Successfully registered");
				} else if (answer == 0) {
					errorLabel.setText("User already exists");
				} else if (answer == 5) {
					errorLabel.setText("Your email is not correct");
				} else if (answer == 4) {
					errorLabel.setText("Your email is already in use");
				} else if (answer == 2) {
					errorLabel.setText("Format of your password/username is wrong");
				} else {
					errorLabel.setText("An error has occurred");
				}
			} else {
				errorLabel.setText("Both passwords do not match");
			}

		}
	}

}
