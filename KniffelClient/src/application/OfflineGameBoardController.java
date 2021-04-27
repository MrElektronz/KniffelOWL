package application;

import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
	@FXML
	private Button btnRoll;
	@FXML
	private Button btnDice1,btnDice2,btnDice3,btnDice4,btnDice5;
	
	private Group root;
	private PerspectiveCamera pc;
	private ArrayList<Dice> dices;
	private ArrayList<Button> diceButtons;
	
	public void initialize() {
		pc = new PerspectiveCamera();
		root = new Group();
		t1.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
		tv1.setItems(getThemes());
		dices = new ArrayList<Dice>();
		diceButtons = new ArrayList<Button>();
		diceButtons.add(btnDice1);
		diceButtons.add(btnDice2);
		diceButtons.add(btnDice3);
		diceButtons.add(btnDice4);
		diceButtons.add(btnDice5);
		
		for(int i = 0; i< 5;i++) {
			dices.add(new Dice());
		root.getChildren().add(dices.get(i));
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
	
	public void onDiceBtnClicked(ActionEvent e) throws IOException {
		if(e.getSource() instanceof Button) {
			Button b = (Button) e.getSource();
			if(!b.getText().equals("-")) {
				int number = Integer.parseInt(b.getText());
				Dice newDice = new Dice(number);
				dices.add(newDice);
				root.getChildren().add(newDice);
				b.setText("-");
			}
		}
	}
	
	public void onNewRoll(ActionEvent e) throws IOException {
		rollDice();
	}
	
	public void onMouseClicked(MouseEvent e) throws IOException {
		Dice d = getNearestDice(e.getX(), e.getY(), 200);
		if(d != null) {
			addDiceToButtons(d);
		}
	}
	
	/**
	 * rolls all dice
	 */
	private void rollDice() {
		for(Dice dice : dices) {
			dice.randomTransforms(dices);
			dice.roll();
		}
	}
	
	/**
	 * removes the dice and adds it to the next free button
	 * @param d
	 */
	private void addDiceToButtons(Dice d) {
		root.getChildren().remove(d);
		dices.remove(d);
		System.out.println("44");
		for(Button b : diceButtons) {
			if(b.getText().equals("-")) {
				b.setText(d.getNumber()+"");
				System.out.println("adasd");
				break;
			}
		}
		
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param maxDistance if the nearest dice is further away than this, null gets returned
	 * @return the nearest dice object from the position of x,y
	 */
	private Dice getNearestDice(double x, double y, float maxDistance) {
		float distance= maxDistance;
		Dice dice = null;
		for(Dice d : dices) {
			if(Point2D.distance(x, y, d.getTranslateX(), d.getTranslateY()) <distance) {
				distance = (float)Point2D.distance(x, y, d.getTranslateX(), d.getTranslateY());
				dice = d;
			}
		}
		return dice;
	}
}
