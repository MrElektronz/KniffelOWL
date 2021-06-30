package application;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
