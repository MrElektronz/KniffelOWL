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
import javafx.scene.paint.Paint;

public class EndScreenController {
	

	@FXML
	private Label text;

	public void setText(String txt,String color) {
		text.setTextFill(Paint.valueOf(color));
		text.setText(txt);
	}
	
	
	/**
	 * @param event
	 * @throws IOException
	 */
	public void onReturn(ActionEvent event) throws IOException {
		Main.changeScene("Scene_MainMenu.fxml",1280,720);
	}
}
