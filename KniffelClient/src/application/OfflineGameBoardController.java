package application;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import client.Client;
import gameplay.Dice;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

public class OfflineGameBoardController {
	
	
	@FXML
	private TableView<String> tv1;
	@FXML
	private TableColumn<String,String> t1;
	@FXML
	private SubScene subScene;
	
	private Group root;
	private PerspectiveCamera pc;
	private Dice[] dices;
	
	public void initialize() {
		pc = new PerspectiveCamera();
		root = new Group();
		t1.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
		tv1.setItems(getThemes());
		dices = new Dice[5];
		for(int i = 0; i< dices.length;i++) {
			dices[i] = new Dice();
		root.getChildren().add(dices[i]);
		}
		subScene.setRoot(root);
		subScene.setCamera(pc);
	}
	
	
	/**
	 * @return ObservableList of Strings ('1','2','sum','3 pasch','yahtzee')
	 */
	public ObservableList<String> getThemes(){
		
		ObservableList<String> themes = FXCollections.observableArrayList();
		themes.add("Aces");
		themes.add("Twos");
		themes.add("Threes");
		themes.add("Fours");
		themes.add("Fives");
		themes.add("Sixes");
		themes.add("Total[Upper]");
		themes.add("Bonus");
		themes.add("Total[Bonus]");
		themes.add(" ");
		themes.add("3 of a kind");
		themes.add("4 of a kind");
		themes.add("Low Straight");
		themes.add("High Straight");
		themes.add("Yahtzee");
		themes.add("Chance");
		themes.add("Total[Lower]");
		themes.add("Grand Total");
		return themes;
	}
	
	
	
	public void onMouseClicked(MouseEvent event) throws IOException {
		System.out.println("click");
		for(Dice dice : dices) {
			dice.randomTransforms(dices);
			dice.roll();
		}
		
	}
}
