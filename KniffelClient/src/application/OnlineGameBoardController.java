package application;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import client.Client;
import de.client.serialize.OnlinePlayerWrapper;
import de.client.serialize.OnlineSessionWrapper;
import gameplay.Dice;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class OnlineGameBoardController{
	
	
	@FXML
	public Label title1,title2,title3,title4;
	
	
	
	private OnlineSessionWrapper session;
	
	//Used to determine what has changed 
	private OnlineSessionWrapper prevSession;
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
	private ArrayList<InteractableCell> iCells = new ArrayList<InteractableCell>();
	
	//So that the player needs to roll at least once to click the dice
	private boolean canClickDice;
	
	//Helps with playing sound effects
	//private SoundGenerator sounds;
	
	private Group root;
	private PerspectiveCamera pc;
	private ArrayList<Dice> dices;
	private ArrayList<Button> diceButtons;
	public void initialize() {
		canClickDice = false;
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
		//sounds = new SoundGenerator();
	}
	
	
	public OnlineSessionWrapper getSession() {
		return session;
	}
	
	public void updateSession(OnlineSessionWrapper session) {
		//Update previous and current session
		this.prevSession = this.session;
		this.session = session;
		
		OnlinePlayerWrapper[] players = session.getPlayers();
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					
					//Update playernames
					for(int i = 0; i< players.length;i++) {
					if(players[i]!=null) {
						if(i==0) {
							title1.setText(players[i].getName());
						}else if(i==1) {
							title2.setText(players[i].getName());
						}else if(i==2) {
							title3.setText(players[i].getName());
						}else if(i==3) {
							title4.setText(players[i].getName());
						}
					}else {
						if(i==0) {
							title1.setText("/");
						}else if(i==1) {
							title2.setText("/");
						}else if(i==2) {
							title3.setText("/");
						}else if(i==3) {
							title4.setText("/");
						}
					}
				}
					
					//roll dice if needed
					if(prevSession != null) {
						
						ArrayList<Integer> oldBank = prevSession.getBank();
						ArrayList<Integer> newBank = session.getBank();
						
						System.out.println("OldBank: "+oldBank.size());
						System.out.println("NewBank: "+newBank.size());
							//Now update the bank (TODO: Not working when only 1 dice left on bank)
							if(!prevSession.getBank().equals(session.getBank())) {

							
							//If dice was removed from bank
							if(oldBank.size()>newBank.size()) {
								System.out.println("NEW BANK:");
								for(Integer d : newBank) {
									System.out.println(d);
								}
								System.out.println(session.serialize());
								int digit = differenceOfPrimitivesArrayList(session.getDice(), prevSession.getDice()).get(0);
								System.out.println("START COMPARISON");
								for(Button b : diceButtons) {
									if(!b.getText().equals("-")) {
										Integer number = Integer.parseInt(b.getText());
										System.out.println(digit +" compared "+number);
										if(digit == number) {
										Dice newDice = new Dice(number);
										dices.add(newDice);
										root.getChildren().add(newDice);
										b.setText("-");
										break;
										}
									}
								}
								
								//If dice was added to bank
								//Do this for the client if active one (so that the clicked dice is being used)
							}else if(oldBank.size()<newBank.size()) {
								//only do for other clients
								if(!Client.getInstance().getUsername().equals(session.getCurrentPlayer().getName())) {
								int number = newBank.get(newBank.size()-1);
								addDiceToButtons(number);
								}
							}
						}else {
							//Bank dice are all the same, if dice lists are not the same, but have the same size(so no dice was added to the bank) then roll
							if(prevSession.getBank().equals(session.getBank())) {
								if(!prevSession.getDice().equals(session.getDice()) && prevSession.getDice().size() == session.getDice().size()) {
									//Roll the dice
									ArrayList<Integer> newNumbers = session.getDice();
									for(int i = 0; i< dices.size();i++) {
										dices.get(i).randomTransforms(dices);
										dices.get(i).setSide(newNumbers.get(i));
										dices.get(i).roll();
									}
									
								}
						}
						}
					}
					
				}
			});
	}
	
	
	
	/**
	 * 
	 * @param <T>
	 * @param a1
	 * @param a2
	 * @return If same length, return a1, if not the same length return new arraylist with all left over elements
	 */
	public <T> ArrayList<T> differenceOfPrimitivesArrayList(ArrayList<T> a1, ArrayList<T> a2){
		ArrayList<T> longer = a1.size()>a2.size() ? (ArrayList<T>) a1.clone() : (ArrayList<T>) a2.clone();
		ArrayList<T> smaller = a1.size()>a2.size() ? (ArrayList<T>) a2.clone() : (ArrayList<T>) a1.clone();
		
		if(longer.equals(smaller)) {
			return a1;
		}
		//2,3,5
		//2,3
		Iterator<T> iter = longer.iterator();
		
		while(iter.hasNext()) {
			T object = iter.next();
			if(smaller.contains(object)) {
				iter.remove();
				smaller.remove(object);
			}
		}
		
		return longer;
	}
	
	
	/**
	 * removes the dice and adds it to the next free button
	 * @param d
	 */
	public void addDiceToButtons(int number) {
		for(Dice d : dices) {
			if(d.getNumber() == number) {
				root.getChildren().remove(d);
				dices.remove(d);
				System.out.println("44");
				for(Button b : diceButtons) {
					if(b.getText().equals("-")) {
						b.setText(number+"");
						System.out.println("adasd");
						break;
			}
		}
			break;
			}
		}
	}
	
	/**
	 * removes the dice and adds it to the next free button
	 * @param d
	 */
	public void addDiceToButtons(Dice d) {
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
	
	
	public void onDiceBtnClicked(ActionEvent e) throws IOException {
		if(session.getCurrentPlayer().getName().equals(Client.getInstance().getUsername())) {
		if(e.getSource() instanceof Button) {
			Button b = (Button) e.getSource();
			//releaseStoredDice(b);
			try {
			int number = Integer.parseInt(b.getText());
			Client.getInstance().requestRemoveBankDice(number);
			}catch(NumberFormatException ex) {
				
			}
		}
		}
	}
	
	
	public void onNewRoll(ActionEvent e) throws IOException {
		//Check if active player before (so the buttons can be greyed out)
		Client.getInstance().requestRollDice();
		
		/*if(gameSession.getCurrentPlayer() == gameSession.getPlayer()) {
		Button b = (Button)e.getSource();
		canClickDice = true;
		rollDice();
		setInteractableButtons(false,false);
    	setInteractableButtons(true,false);
		
		}*/
	}
	
	public void onMouseClicked(MouseEvent e) throws IOException {
																						//&& canClickDice
		if(session.getCurrentPlayer().getName().equals(Client.getInstance().getUsername())) {
		Dice d = getNearestDice(e.getX(), e.getY(), 200);
		if(d != null) {
			addDiceToButtons(d);
			Client.getInstance().requestAddBankDice(d.getNumber());
		}
		}
	}

	
	private void releaseStoredDice(Button b) {
		if(!b.getText().equals("-")) {
			int number = Integer.parseInt(b.getText());
			Dice newDice = new Dice(number);
			dices.add(newDice);
			root.getChildren().add(newDice);
			b.setText("-");
			Client.getInstance().requestRemoveBankDice(number);
		}
	}
	
	private void releaseAllStoredDice() {
		for(Button btn : diceButtons) {
			releaseStoredDice(btn);
		}
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
	            		//gameSession.nextTurn();
	            	}
	            });
	        }
	        
	        
	        public void setInteractable(boolean interact, boolean clicked) {
	        	int id = iCells.indexOf(this);
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
	    			System.out.println("id "+id+" set to "+sum);
	    		}
	    	/*	//three of a kind
	    		else if(id == 10) {
	    			button.setText(isThreeOfAKind(allDice)+"");
	    		}
	    		//four of a kind
	    		else if(id == 11) {
	    			button.setText(isFourOfAKind(allDice)+"");
	    		}
	    		else if(id == 12) {
	    			button.setText(isFullHouse(allDice)+"");
	    		}
	    		else if(id == 13) {
	    			button.setText(isLowStraight(allDice)+"");
	    		}
	    		else if(id == 14) {
	    			button.setText(isHighStraight(allDice)+"");
	    		}
	    		else if(id == 15) {
	    			button.setText(isYahtzee(allDice)+"");
	    		}*/
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

