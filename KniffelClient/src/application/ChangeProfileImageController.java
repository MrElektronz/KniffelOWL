package application;

import client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import profile.ProfileManager;

public class ChangeProfileImageController {
	

	@FXML
	private HBox box;
	
	public void initialize() {
		Button[] btns = new Button[5];
		for(int i = 0; i<btns.length;i++) {
			Button b = new Button(i+"",new ImageView(ProfileManager.getInstance().getImage(i)));
			b.setStyle("-fx-background-color: transparent; -fx-text-fill: transparent");
			b.setOnMouseClicked(event ->{
				int id = Integer.parseInt(b.getText());
				Client.getInstance().setProfilePicID(id);
				Main.mainMenu.updateProfileImage();
				Client.getInstance().sendNewProfilePicID(id);
			});
			btns[i] = b;
		}
		box.getChildren().addAll(btns);
	}
	
}
