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

	
	
	
	public void onBackToLogin(ActionEvent event) throws IOException {
		Main.changeScene("Scene_Login.fxml");
	}
	
	public void onSendCode(ActionEvent event) throws IOException {
		if(username.getText().length()<3) {
			errorLabel.setText("username not found");
		}
		byte result = Client.requestResetPassword(username.getText());
		if(result == 0) {
			errorLabel.setText("username not found");
		}else if(result == 2) {
			errorLabel.setText("An error has occurred");
		}else if(result == 1){
			errorLabel.setText("An email with the reset code has been send");
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
				int answer = Client.resetPassword(username.getText(), password.getText(), pin.getText());
				if(answer == 1) {
					errorLabel.setText("Password was changed successfully");
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
