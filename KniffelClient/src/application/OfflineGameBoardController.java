package application;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import gameplay.Bot;
import gameplay.Dice;
import gameplay.OfflinePlayer;
import gameplay.OfflineSession;
import gameplay.Bot.BotDifficulty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import sound.SoundGenerator;

public class OfflineGameBoardController {
	
	
	@FXML
	private TableView<String> tv1,tv3,tv4,tv5;
	@FXML
	private TableView<String> tv2;
	@FXML
	private TableColumn<String,String> t1,t3,t4,t5;
	@FXML
	private TableColumn<String, Void>t2;
	@FXML
	private SubScene subScene;
	@FXML
	private Button btnRoll;
	@FXML
	private Button btnDice1,btnDice2,btnDice3,btnDice4,btnDice5;
	@FXML
	private GridPane gridPane;
	
	//Collection of all interactable cells
	private ArrayList<InteractableCell> iCells = new ArrayList<OfflineGameBoardController.InteractableCell>();
	
	//So that the player needs to roll at least once to click the dice
	private boolean canClickDice;
	
	//Helps with playing sound effects
	private SoundGenerator sounds;
	
	private Group root;
	private PerspectiveCamera pc;
	private ArrayList<Dice> dices;
	private ArrayList<Button> diceButtons;
	private OfflineSession gameSession;
	private YahtzeeHelper yh;
	
	public void initialize() {
		yh = YahtzeeHelper.getInstance();
		canClickDice = false;
		gameSession = new OfflineSession(Bot.BotDifficulty.EASY,this);
		pc = new PerspectiveCamera();
		root = new Group();
		t1.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
		t2.setCellFactory(col -> new InteractableCell());
		t3.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
		t4.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
		t5.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));

		
		tv1.setItems(getThemes());
		tv2.setItems(fillZeroes());
		tv3.setItems(fillZeroes());
		tv4.setItems(fillZeroes());
		tv5.setItems(fillZeroes());
		
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
		
		//add player/bots
		//gameSession.setPlayer(new OfflinePlayer(Client.requestUsername()));
		gameSession.setPlayer(new OfflinePlayer("p1"));
		sounds = new SoundGenerator();
	}
	
	
	public void setDifficulty(BotDifficulty difficulty) {
		gameSession.getBot().setDifficulty(difficulty);
	}
	
	/**
	 * 
	 * @param interact if true all buttons which are possible to be interacted with will get interactable
	 */
	public void setInteractableButtons(boolean interact, boolean clicked) {
		for(InteractableCell ic : iCells) {
			ic.setInteractable(interact,clicked);
		}
	}
	
	public ObservableList<String> fillZeroes(){
		ObservableList<String> entry = FXCollections.observableArrayList();
		for(int i = 0; i<9;i++) {
			entry.add("0");
		}
		entry.add(" ");
		for(int i = 0; i<9;i++) {
			entry.add("0");
		}
		return entry;
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
		themes.add("Full House");
		themes.add("Low Straight");
		themes.add("High Straight");
		themes.add("Yahtzee");
		themes.add("Chance");
		themes.add("Total[Lower]");
		themes.add("Grand Total");
		return themes;
	}
	
	public void endGame() {
		int playerTotal = Integer.parseInt(iCells.get(18).button.getText());
		int botTotal = Integer.parseInt(tv3.getItems().get(18));
		if(playerTotal > botTotal) {
			try {
				Main.changeToEndScreen("You have won", "#CC8000");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(playerTotal == botTotal) {
			try {
				Main.changeToEndScreen("It is a draw", "#DDDD");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			try {
				Main.changeToEndScreen("You have lost", "#CC1F00");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void onDiceBtnClicked(ActionEvent e) throws IOException {
		if(gameSession.getCurrentPlayer() == gameSession.getPlayer()) {
		if(e.getSource() instanceof Button) {
			Button b = (Button) e.getSource();
			releaseStoredDice(b);
		}
		}
	}
	
	public void releaseStoredDice(Button b) {
		if(!b.getText().equals("-")) {
			int number = Integer.parseInt(b.getText());
			Dice newDice = new Dice(number);
			dices.add(newDice);
			root.getChildren().add(newDice);
			b.setText("-");
		}
	}
	
	public void onNewRoll(ActionEvent e) throws IOException {
		if(gameSession.getCurrentPlayer() == gameSession.getPlayer()) {
		Button b = (Button)e.getSource();
		canClickDice = true;
		rollDice();
		setInteractableButtons(false,false);
    	setInteractableButtons(true,false);
		
		}
	}
	
	public void onMouseClicked(MouseEvent e) throws IOException {
		if(gameSession.getCurrentPlayer() == gameSession.getPlayer() && canClickDice) {
		Dice d = getNearestDice(e.getX(), e.getY(), 200);
		if(d != null) {
			addDiceToButtons(d);
		}
		}
	}
	
	/**
	 * rolls all dice
	 */
	public void rollDice() {
		if(gameSession.getCurrentRoll() == 3) {
			btnRoll.setVisible(false);
		}
		sounds.playRoll();
		gameSession.nextRoll();
		btnRoll.setText(getButtonLabel());
		for(Dice dice : dices) {
			dice.randomTransforms(dices);
			dice.roll();
		}
	}
	
	private String getButtonLabel() {
		
		switch(gameSession.getCurrentRoll()) {
		case 1: return "1st Roll";
		case 2: return "2th Roll";
		case 3: return "3rd Roll";
		}
		return "? Roll";
	}
	
	/**
	 * removes the dice and adds it to the next free button
	 * @param d
	 */
	public void addDiceToButtons(Dice d) {
		root.getChildren().remove(d);
		dices.remove(d);
		for(Button b : diceButtons) {
			if(b.getText().equals("-")) {
				b.setText(d.getNumber()+"");
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

	/**
	 * 
	 * @return all dice numbers of 3D-dice or stored ones
	 */
	public ArrayList<Integer> getAllDices(){
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		for(Dice d : dices) {
			if(d.isVisible()) {
				numbers.add(d.getNumber());
			}
		}
		for(Button btn : diceButtons) {
			if(!btn.getText().equals("-"))
			numbers.add(Integer.parseInt(btn.getText()));
		}
		return numbers;
	}
	
	//The following methods only exist so that they can be used for the bot's logic//
	
	public ArrayList<Dice> getDices() {
		return dices;
	}
	public ArrayList<Button> getDiceButtons() {
		return diceButtons;
	}
	public ArrayList<InteractableCell> getiCells() {
		return iCells;
	}
	
	/////////////////////////////////////////////////////////////////////////////
	
	public void switchCurrentPlayer(boolean humanPlayer) {
		btnRoll.setVisible(true);
		btnRoll.setText(getButtonLabel());
		if(humanPlayer) {
			btnRoll.setStyle("");
			btnDice1.setStyle("");
			btnDice2.setStyle("");
			btnDice3.setStyle("");
			btnDice4.setStyle("");
			btnDice5.setStyle("");
		}else {
			String style = "-fx-background-color: "+gameSession.getBot().getColor();
			btnRoll.setStyle(style);
			btnDice1.setStyle(style);
			btnDice2.setStyle(style);
			btnDice3.setStyle(style);
			btnDice4.setStyle(style);
			btnDice5.setStyle(style);
			gameSession.getBot().start();
		}
		updateTotalLabelsBot();
		updateTotalLabelsPlayer();
	}
	
	public void releaseAllStoredDice() {
		for(Button btn : diceButtons) {
			releaseStoredDice(btn);
		}
	}
	/**
	 * only used for the bot, scores of player is stored in a different way
	 * the list is filled with the scores of the current roll+stored dice, special fields like bonus and total are unchanged and can't
	 * be written to
	 * @return list of scores, index 0: aces, index 1: twos
	 */
	public ArrayList<Integer> getScores(){
		ArrayList<Integer> scores = new ArrayList<Integer>();
		ArrayList<Integer> allDice = getAllDices();
		for(int id = 0; id<6;id++) {
		int sum = 0;
		if(id < 6) {
			sum = 0;
			for(int i : allDice) {
				if(i==id+1) {
					sum+=(id+1);
				}
			}
		}
		scores.add(sum);
		}
		ObservableList<String> entries = tv3.getItems();
		scores.add(Integer.parseInt(entries.get(6)));
		scores.add(Integer.parseInt(entries.get(7)));
		scores.add(Integer.parseInt(entries.get(8)));
		//empty line
		scores.add(-1);
		scores.add(yh.isThreeOfAKind(allDice));
		scores.add(yh.isFourOfAKind(allDice));
		scores.add(yh.isFullHouse(allDice));
		scores.add(yh.isLowStraight(allDice));
		scores.add(yh.isHighStraight(allDice));
		scores.add(yh.isYahtzee(allDice));
		
		int chance = 0;
		for(int i : allDice) {
			chance+=i;
		}
		scores.add(chance);
		scores.add(Integer.parseInt(entries.get(17)));
		scores.add(Integer.parseInt(entries.get(18)));
		
		
		return scores;
	}
	public boolean isScoreAlreadySet(int id){
		ObservableList<String> entries = tv3.getItems();
		return !(entries.get(id).equals("0") || entries.get(id).contentEquals(" "));
	}
	
	private void updateTotalLabelsBot() {
		ObservableList<String> entries = tv3.getItems();
		int totalUpperSum = 0;
		for(int i = 0; i<6;i++) {
			totalUpperSum+=Integer.parseInt(entries.get(i));
		}
		entries.set(6, totalUpperSum+"");
		if(totalUpperSum>62) {
			entries.set(7,"35");
			entries.set(8, (totalUpperSum+35)+"");
		}else {
			entries.set(8, (totalUpperSum)+"");
		}
		int totalLowerSum = 0;
		for(int i = 10;i<17;i++) {
			totalLowerSum+=Integer.parseInt(entries.get(i));
		}
		entries.set(17, totalLowerSum+"");

		if(totalUpperSum > 62) {
			entries.set(18,totalUpperSum+totalLowerSum+35+"");
			}else {
				entries.set(18,totalUpperSum+totalLowerSum+"");
			}
		
		tv3.setItems(entries);
	}
	
	private void updateTotalLabelsPlayer() {
		InteractableCell totalUpper = iCells.get(6);
		InteractableCell bonus = iCells.get(7);
		InteractableCell totalBonus = iCells.get(8);
		InteractableCell totalLower = iCells.get(17);
		InteractableCell grandTotal = iCells.get(18);
		int totalUpperSum = 0;
		for(int i = 0;i<6;i++) {
			totalUpperSum+=Integer.parseInt(iCells.get(i).button.getText());
		}
		totalUpper.button.setText(totalUpperSum+"");
		if(totalUpperSum>62) {
			bonus.button.setText("35");
			totalBonus.button.setText((totalUpperSum+35)+"");
		}else {
			totalBonus.button.setText((totalUpperSum)+"");
		}
		
		int totalLowerSum = 0;
		for(int i = 10;i<17;i++) {
			totalLowerSum+=Integer.parseInt(iCells.get(i).button.getText());
		}
		totalLower.button.setText(totalLowerSum+"");
		if(totalUpperSum > 62) {
		grandTotal.button.setText(totalUpperSum+totalLowerSum+35+"");
		}else {
			grandTotal.button.setText(totalUpperSum+totalLowerSum+"");
		}
	}
	
	
	
	/**
	 * 
	 * @param id if id=0 'aces' are chosen, if id=1 'twos' are chosen
	 * @throws Exception 
	 */
	public void chooseScore(int id, ArrayList<Integer> scores) {
		if(!isChoosableScoreID(id)) {
			System.err.println(id+" is no choosable score id, use method 'isChoosableScoreID(id) to check'");
		}
		ObservableList<String> entries = tv3.getItems();
		entries.set(id, scores.get(id)+"");
		
		tv3.setItems(entries);
		releaseAllStoredDice();
		canClickDice = false;
		gameSession.nextTurn();
	}
	
	public boolean isChoosableScoreID(int id) {
		return !((id>5 && id <10) || id > 16);		
	}
	
	
	class InteractableCell extends TableCell<String, Void>{
		 private final Button button;
		 private boolean interactable = false;
		 //needed to store the previous number of the button, if not selected
		 private int prevNum = 0;
		 boolean wasPressed = false;
		 public InteractableCell() {
			 iCells.add(this);
		}
		 
	        {
	            button = new Button("0");
	            button.setMinWidth(86);
	            button.setOnAction(ev -> {
	            	if(interactable) {
	            		wasPressed = true;
	            		releaseAllStoredDice();
	            		for(InteractableCell ic : iCells) {
	            			if(ic != this && ic.interactable) {
	            				ic.button.setText("0");
	            			}
	            		}
	            		setInteractableButtons(false,true);
	            		gameSession.nextTurn();
	            	}
	            });
	        }
	        
	        
	        public void setInteractable(boolean interact, boolean clicked) {
	        	//lock in clicked number
	        	if(wasPressed) {
	        		interact = false;
	        		clicked = false;
	        	}
	        	//if is already interactable (or not) do nothing
	        	if(interactable == interact || isSpecialCell()) {
	        		return;
	        	}
	        	interactable = interact;
	        	prevNum = Integer.parseInt(button.getText());
	        	if(interactable) {
		            button.setStyle("-fx-background-color: #FAEF25");
		            button.setMinHeight(30);
		            setCellNumber(getAllDices());
	        	}else {
		            button.setStyle("-fx-background-color: #FFF");
		            button.setMinHeight(-1);
		            if(!clicked) {
	            		for(InteractableCell ic : iCells) {
	            			if(!ic.isSpecialCell())
	            				ic.button.setText(ic.prevNum+"");
	            		}
		            }
	        	}
	        }

	        @Override
	        protected void updateItem(Void item, boolean empty) {
	            super.updateItem(item, empty);
	            setGraphic(empty ? null : button);
	            //button.setStyle("-fx-background-color: #AAA");
	        }
	        
	        public void setCellNumber(ArrayList<Integer> allDice) {
	    		int id = iCells.indexOf(this);
	    		
	    		//check if already set
	    		if(!wasPressed) {
    			
	    		if(id < 6) {
	    			int sum = 0;
	    			for(int i : allDice) {
	    				if(i==id+1) {
	    					sum+=(id+1);
	    				}
	    			}
	    			button.setText(sum+"");
	    		}
	    		//three of a kind
	    		else if(id == 10) {
	    			button.setText(yh.isThreeOfAKind(allDice)+"");
	    		}
	    		//four of a kind
	    		else if(id == 11) {
	    			button.setText(yh.isFourOfAKind(allDice)+"");
	    		}
	    		else if(id == 12) {
	    			button.setText(yh.isFullHouse(allDice)+"");
	    		}
	    		else if(id == 13) {
	    			button.setText(yh.isLowStraight(allDice)+"");
	    		}
	    		else if(id == 14) {
	    			button.setText(yh.isHighStraight(allDice)+"");
	    		}
	    		else if(id == 15) {
	    			button.setText(yh.isYahtzee(allDice)+"");
	    		}
	    		//chance
	    		else if(id == 16) {
	    			int sum = 0;
	    			for(int i : allDice) {
	    				sum+=i;
	    			}
	    			button.setText(sum+"");
	    		}
	    		}
	    	}
	        /**
	         * 
	         * @return true if cell is non-interactable cell
	         */
	        private boolean isSpecialCell() {
	    		int id = iCells.indexOf(this);
	    		return ((id>5 && id <10) || id > 16);		
	        }
	}
	
	
}

