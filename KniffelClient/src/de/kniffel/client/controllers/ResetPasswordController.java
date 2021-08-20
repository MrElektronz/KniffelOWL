package de.kniffel.client.controllers;

import java.io.IOException;

import de.kniffel.client.main.ClientMain;
import de.kniffel.client.utils.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * 
 * @author KBeck
 * 
 *         Controller class of the ResetPW-Scene. Lets the user reset the
 *         password of an existing account.
 *
 */

public class ResetPasswordController {

	@FXML
	private Label errorLabel;
	@FXML
	private TextField username;
	@FXML
	private TextField emailCode;
	@FXML
	private TextField pin;
	@FXML
	private PasswordField password;
	@FXML
	private PasswordField password2;
	@FXML
	private Label lCheckMail;

	public void onBackToLogin(ActionEvent event) throws IOException {
		ClientMain.changeScene("Scene_Login.fxml");
	}

	public void onSendCode(ActionEvent event) throws IOException {
		if (username.getText().length() < 3) {
			lCheckMail.setVisible(false);
			errorLabel.setVisible(true);
			errorLabel.setText("username not found");
		}
		byte result = Client.getInstance().requestResetPassword(username.getText());
		if (result == 0) {
			lCheckMail.setVisible(false);
			errorLabel.setVisible(true);
			errorLabel.setText("username not found");
		} else if (result == 2) {
			lCheckMail.setVisible(false);
			errorLabel.setVisible(true);
			errorLabel.setText("An error has occurred");
		} else if (result == 1) {
			lCheckMail.setText("An email with the reset code has been send to your e-mail address");
			lCheckMail.setVisible(true);
			errorLabel.setVisible(false);
		}

	}

	/**
	 * handles the process after the 'Login'-Button has been clicked
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onSetNewPassword(ActionEvent event) throws IOException {
		if (username.getText().length() > 1 && password2.getText().length() > 1 && password.getText().length() > 1
				&& pin.getText().length() > 1) {
			if (password.getText().equals(password2.getText())) {
				int answer = Client.getInstance().resetPassword(username.getText(), password.getText(), pin.getText());
				if (answer == 1) {
					lCheckMail.setText("Password was changed successfully");
					lCheckMail.setVisible(true);
					errorLabel.setVisible(false);
				} else if (answer == 0) {
					lCheckMail.setVisible(false);
					errorLabel.setVisible(true);
					errorLabel.setText("User does not exist");
				} else if (answer == 3) {
					lCheckMail.setVisible(false);
					errorLabel.setVisible(true);
					errorLabel.setText("Your pin is wrong");
				} else {
					lCheckMail.setVisible(false);
					errorLabel.setVisible(true);
					errorLabel.setText("An error has occurred[" + answer + "]");
				}
			} else {
				lCheckMail.setVisible(false);
				errorLabel.setText("Both passwords do not match");
			}

		}
	}

}
