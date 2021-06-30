package application;

import java.io.IOException;

import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
	
	
	/**
	 * gets called on startup, automatically enters username and password if possible
	 */
	public void initialize() {

		this.username.setText(Main.readProperty("client.user"));
		this.password.setText(Main.readProperty("client.password"));
	}

	
	public void onRegisterHere(ActionEvent event) throws IOException {
		Main.changeScene("Scene_Register.fxml");
	}
	
	public void onForgotPassword(ActionEvent event) throws IOException {
		Main.changeScene("Scene_ResetPW.fxml");
	}
	
	
	/**
	 * handles the process after the 'Login'-Button has been clicked
	 * @param event
	 * @throws IOException
	 */
	public void onLogin(ActionEvent event) throws IOException {
		if(username.getText() != null && password.getText() != null) {
			
			if(keepSignedIn.isSelected()) {
				
				
		            Main.updateUserPassword(username.getText(), password.getText());
		            System.out.println("saved");
			}

			String sessionID = Client.login(username.getText(), password.getText());
			
			if(sessionID == null) {
				errorLabel.setText("Ein Fehler ist aufgetreten");
			}else if(sessionID.equals("0")) {
				errorLabel.setText("Der Nutzername/Password ist falsch");
			}else if(sessionID.equals("-1")) {
				errorLabel.setText("Dieser Nutzer ist bereits eingeloggt");
			}else {
				errorLabel.setText("Du wurdest erfolgreich eingeloggt");
				Client.setUsername(username.getText());
				Client.updateProfilePic();
				Main.changeScene("Scene_MainMenu.fxml",1280,720);
			}
			}
		
	}
}
