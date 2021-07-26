package application;

import java.io.IOException;

import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
		Main.changeScene("Scene_Login.fxml");
	}
	
	public void onSendCode(ActionEvent event) throws IOException {
		if(username.getText().length()<3) {
			errorLabel.setText("username not found");
		}
		byte result = Client.getInstance().requestResetPassword(username.getText());
		if(result == 0) {
			errorLabel.setText("username not found");
		}else if(result == 2) {
			errorLabel.setText("An error has occurred");
		}else if(result == 1){
			lCheckMail.setText("An email with the reset code has been send to your e-mail address");
			lCheckMail.setVisible(true);
		}
		
	}
	
	
	/**
	 * handles the process after the 'Login'-Button has been clicked
	 * @param event
	 * @throws IOException
	 */
	public void onSetNewPassword(ActionEvent event) throws IOException {
		if(username.getText().length()>1&& password2.getText().length()>1&&password.getText().length()>1&&pin.getText().length()>1) {
			if(password.getText().equals(password2.getText())) {
				int answer = Client.getInstance().resetPassword(username.getText(), password.getText(), pin.getText());
				if(answer == 1) {
					lCheckMail.setText("Password was changed successfully");
					lCheckMail.setVisible(true);
				}else if(answer == 0) {
					errorLabel.setText("User does not exist");
				}else if(answer == 3) {
					errorLabel.setText("Your pin is wrong");
				}else {
					errorLabel.setText("An error has occurred["+answer+"]");
				}
			}else {
				errorLabel.setText("Both passwords do not match");
			}
		
		
		}
	}
	
}
