package de.kniffel.client.controllers;

import java.io.IOException;

import de.kniffel.client.main.ClientMain;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;

/**
 * Controller class of the End-Scene Pretty much only functionality is:
 * Displaying a win/loss text and adding a "Return to main menu"-button.
 * 
 * @author KBeck
 * 
 */
public class EndScreenController {

	@FXML
	private Label text;

	/**
	 * Set text and color of it to the corresponding parameters.
	 * 
	 * @param txt   to display
	 * @param color of the text
	 */
	public void setText(String txt, String color) {
		text.setTextFill(Paint.valueOf(color));
		text.setText(txt);
	}

	/**
	 * Gets called when the button was clicked.
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onReturn(ActionEvent event) throws IOException {
		ClientMain.gameEnded = false;
		ClientMain.changeScene("Scene_MainMenu.fxml", 1280, 720);
	}
}
