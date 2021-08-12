package de.kniffel.client.controllers;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import de.kniffel.client.gameplay.Dice;
import de.kniffel.client.serialize.OnlinePlayerWrapper;
import de.kniffel.client.serialize.OnlineSessionWrapper;
import de.kniffel.client.sound.SoundGenerator;
import de.kniffel.client.utils.Client;
import de.kniffel.client.utils.YahtzeeHelper;
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

public class OnlineGameBoardController {

	@FXML
	public Label title1, title2, title3, title4;
	@FXML
	private TableView<String> tv1, tv3, tv4, tv5;
	@FXML
	private TableView<String> tv2;
	@FXML
	private TableColumn<String, Void> t2, t3, t4, t5;
	@FXML
	private TableColumn<String, String> t1;
	@FXML
	private SubScene subScene;
	@FXML
	private Button btnRoll;
	@FXML
	private Button btnDice1, btnDice2, btnDice3, btnDice4, btnDice5;
	@FXML
	private GridPane gridPane;

	// Collections of all interactable cells of all players
	private ArrayList<InteractableCell> iCells1, iCells2, iCells3, iCells4;

	// Helps with playing sound effects
	private SoundGenerator sounds;

	// These two references are used to help displaying the 3D-Dice
	private Group root;
	private PerspectiveCamera pc;

	private ArrayList<Dice> diceList;
	private ArrayList<Button> diceButtons;

	private YahtzeeHelper yh;
	private OnlineSessionWrapper session;
	// Used to determine what has changed
	private OnlineSessionWrapper prevSession;

	public void initialize() {
		yh = YahtzeeHelper.getInstance();
		pc = new PerspectiveCamera();
		root = new Group();

		iCells1 = new ArrayList<InteractableCell>();
		iCells2 = new ArrayList<InteractableCell>();
		iCells3 = new ArrayList<InteractableCell>();
		iCells4 = new ArrayList<InteractableCell>();

		t1.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
		t2.setCellFactory(col -> new InteractableCell(iCells1));
		t3.setCellFactory(col -> new InteractableCell(iCells2));
		t4.setCellFactory(col -> new InteractableCell(iCells3));
		t5.setCellFactory(col -> new InteractableCell(iCells4));

		// Fill the tables with zeroes at the start
		tv1.setItems(getThemes());
		tv2.setItems(fillZeroes());
		tv3.setItems(fillZeroes());
		tv4.setItems(fillZeroes());
		tv5.setItems(fillZeroes());

		diceList = new ArrayList<Dice>();
		diceButtons = new ArrayList<Button>();

		// Add buttons to list for easier handling
		diceButtons.add(btnDice1);
		diceButtons.add(btnDice2);
		diceButtons.add(btnDice3);
		diceButtons.add(btnDice4);
		diceButtons.add(btnDice5);

		for (int i = 0; i < 5; i++) {
			diceList.add(new Dice());
			root.getChildren().add(diceList.get(i));
		}

		subScene.setRoot(root);
		subScene.setCamera(pc);

		sounds = new SoundGenerator();
	}

	/**
	 * This gets called whenever the client receives a new game session update and
	 * handles updating the current session and display to the new one. This
	 * includes for example: rolling the dice when they are being rolled on the
	 * server, updating the scores and (de)spawning dice.
	 * 
	 * @param session new session
	 */
	public void updateSession(OnlineSessionWrapper session) {

		if (prevSession == null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					updateCurrentPlayerButtons();
				}

			});
		}

		// Update previous and current session
		this.prevSession = this.session;
		this.session = session;

		OnlinePlayerWrapper[] players = session.getPlayers();
		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				// Update playernames
				for (int i = 0; i < players.length; i++) {
					if (players[i] != null) {
						if (i == 0) {
							title1.setText(players[i].getName());
						} else if (i == 1) {
							title2.setText(players[i].getName());
						} else if (i == 2) {
							title3.setText(players[i].getName());
						} else if (i == 3) {
							title4.setText(players[i].getName());
						}
					} else {
						if (i == 0) {
							title1.setText("/");
						} else if (i == 1) {
							title2.setText("/");
						} else if (i == 2) {
							title3.setText("/");
						} else if (i == 3) {
							title4.setText("/");
						}
					}
				}

				// roll dice if needed
				if (prevSession != null) {

					ArrayList<Integer> oldBank = prevSession.getBank();
					ArrayList<Integer> newBank = session.getBank();

					// Now update the bank
					if (!prevSession.getBank().equals(session.getBank())) {

						// If dice was removed from bank
						if (oldBank.size() > newBank.size()) {

							int digit = differenceOfPrimitivesArrayList(session.getDice(), prevSession.getDice())
									.get(0);
							for (Button b : diceButtons) {
								if (!b.getText().equals("-")) {
									Integer number = Integer.parseInt(b.getText());
									if (digit == number) {
										Dice newDice = new Dice(number);
										diceList.add(newDice);
										root.getChildren().add(newDice);
										b.setText("-");
										break;
									}
								}
							}

							// If dice was added to bank
							// Do this for the client if active one (so that the clicked dice is being used)
						} else if (oldBank.size() < newBank.size()) {
							// only do for other clients
							if (!Client.getInstance().getUsername().equals(session.getCurrentPlayer().getName())) {
								int number = newBank.get(newBank.size() - 1);
								addDiceToButtons(number);
							}
						}
					} else {
						// Bank dice are all the same, if dice lists are not the same, but have the same
						// size(so no dice was added to the bank) then roll
						if (prevSession.getBank().equals(session.getBank())) {
							if (!prevSession.getDice().equals(session.getDice())
									&& prevSession.getDice().size() == session.getDice().size()) {

								// Roll the dice
								ArrayList<Integer> newNumbers = session.getDice();
								for (int i = 0; i < diceList.size(); i++) {
									diceList.get(i).randomTransforms(diceList);
									diceList.get(i).setSide(newNumbers.get(i));
									diceList.get(i).roll();
								}
								sounds.playRoll();

								// Update score buttons after roll
								if (session.getCurrentPlayer().getName().equals(Client.getInstance().getUsername())) {
									setInteractableButtons(false, false);
									setInteractableButtons(true, false);
								}

							}
						}
					}

					// set player scores
					if (session.getPlayer(0) != null) {
						int[] scores = session.getPlayer(0).getScores();
						for (int i = 0; i < scores.length; i++) {
							int id = i;
							if (id > 5) {
								id += 4;
							}
							iCells1.get(id).setCellScore(scores[i]);
						}
					}

					if (session.getPlayer(1) != null) {
						int[] scores = session.getPlayer(1).getScores();
						for (int i = 0; i < scores.length; i++) {
							int id = i;
							if (id > 5) {
								id += 4;
							}
							iCells2.get(id).setCellScore(scores[i]);
						}
					}

					if (session.getPlayer(2) != null) {
						int[] scores = session.getPlayer(2).getScores();
						for (int i = 0; i < scores.length; i++) {
							int id = i;
							if (id > 5) {
								id += 4;
							}
							iCells3.get(id).setCellScore(scores[i]);
						}
					}

					if (session.getPlayer(3) != null) {
						int[] scores = session.getPlayer(3).getScores();
						for (int i = 0; i < scores.length; i++) {
							int id = i;
							if (id > 5) {
								id += 4;
							}
							iCells4.get(id).setCellScore(scores[i]);
						}
					}

					// Update total score
					for (OnlinePlayerWrapper p : players) {
						if (p != null) {
							updateTotalLabelsPlayer(p);
						}
					}

					// Update roll button
					btnRoll.setText(getButtonLabel());

					// Update button color for current player
					if (session.getPlayerCount() > 1 && (prevSession.getCurrentPlayer() == null || !prevSession
							.getCurrentPlayer().getName().equals(session.getCurrentPlayer().getName()))) {
						updateCurrentPlayerButtons();
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
	 * @return If same length, return a1, if not the same length return new
	 *         arraylist with all left over elements
	 */
	@SuppressWarnings("unchecked")
	public <T> ArrayList<T> differenceOfPrimitivesArrayList(ArrayList<T> a1, ArrayList<T> a2) {
		ArrayList<T> longer = a1.size() > a2.size() ? (ArrayList<T>) a1.clone() : (ArrayList<T>) a2.clone();
		ArrayList<T> smaller = a1.size() > a2.size() ? (ArrayList<T>) a2.clone() : (ArrayList<T>) a1.clone();

		if (longer.equals(smaller)) {
			return a1;
		}
		// 2,3,5
		// 2,3
		Iterator<T> iter = longer.iterator();

		while (iter.hasNext()) {
			T object = iter.next();
			if (smaller.contains(object)) {
				iter.remove();
				smaller.remove(object);
			}
		}

		return longer;
	}

	/**
	 * removes the dice and adds it to the next free button
	 * 
	 * @param d
	 */
	public void addDiceToButtons(int number) {
		for (Dice d : diceList) {
			if (d.getNumber() == number) {
				root.getChildren().remove(d);
				diceList.remove(d);
				for (Button b : diceButtons) {
					if (b.getText().equals("-")) {
						b.setText(number + "");
						break;
					}
				}
				break;
			}
		}
	}

	/**
	 * removes the dice and adds it to the next free button
	 * 
	 * @param d
	 */
	public void addDiceToButtons(Dice d) {
		root.getChildren().remove(d);
		diceList.remove(d);
		for (Button b : diceButtons) {
			if (b.getText().equals("-")) {
				b.setText(d.getNumber() + "");
				break;
			}
		}
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param maxDistance if the nearest dice is further away than this, null gets
	 *                    returned
	 * @return the nearest dice object from the position of x,y
	 */
	private Dice getNearestDice(double x, double y, float maxDistance) {
		float distance = maxDistance;
		Dice dice = null;
		for (Dice d : diceList) {
			if (Point2D.distance(x, y, d.getTranslateX(), d.getTranslateY()) < distance) {
				distance = (float) Point2D.distance(x, y, d.getTranslateX(), d.getTranslateY());
				dice = d;
			}
		}
		return dice;
	}

	/**
	 * 
	 * @return ObservableList of needed size, filled with zeroes as Strings
	 */
	public ObservableList<String> fillZeroes() {
		ObservableList<String> entry = FXCollections.observableArrayList();
		for (int i = 0; i < 9; i++) {
			entry.add("0");
		}
		entry.add(" ");
		for (int i = 0; i < 9; i++) {
			entry.add("0");
		}
		return entry;
	}

	/**
	 * @return ObservableList of Strings ('1','2','sum','3 pasch','yahtzee')
	 */
	public ObservableList<String> getThemes() {

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

	/**
	 * Removes dice from clicked button and add it to the playing field as 3D-Object
	 * 
	 * @param e
	 * @throws IOException
	 */
	public void onDiceBtnClicked(ActionEvent e) throws IOException {
		if (session.getCurrentPlayer().getName().equals(Client.getInstance().getUsername())) {
			if (e.getSource() instanceof Button) {
				Button b = (Button) e.getSource();
				try {
					int number = Integer.parseInt(b.getText());
					Client.getInstance().requestRemoveBankDice(number);
				} catch (NumberFormatException ex) {

				}
			}
		}
	}

	/**
	 * Tells the server to roll the dice
	 * 
	 * @param e
	 * @throws IOException
	 */
	public void onNewRoll(ActionEvent e) throws IOException {
		if (session.getCurrentPlayer().getName().equals(Client.getInstance().getUsername())) {
			Client.getInstance().requestRollDice();
		}
	}

	/**
	 * Add a 3D-Dice to the bank of stored dice
	 * 
	 * @param e
	 * @throws IOException
	 */
	public void onMouseClicked(MouseEvent e) throws IOException {

		if (session.getCurrentPlayer().getName().equals(Client.getInstance().getUsername())) {
			Dice d = getNearestDice(e.getX(), e.getY(), 200);
			if (d != null && session.getCurrentRoll() > 1 && session.getCurrentRoll() < 4) {
				addDiceToButtons(d);
				Client.getInstance().requestAddBankDice(d.getNumber());
			}
		}
	}

	/**
	 * @param b Moves a specific stored dice from Button "b" to the playing field as
	 *          3D-Objects
	 */
	private void releaseStoredDice(Button b) {
		if (!b.getText().equals("-")) {
			int number = Integer.parseInt(b.getText());
			Dice newDice = new Dice(number);
			diceList.add(newDice);
			root.getChildren().add(newDice);
			b.setText("-");
			Client.getInstance().requestRemoveBankDice(number);
		}
	}

	/**
	 * Moves all stored dice to the playing field as 3D-Objects
	 */
	private void releaseAllStoredDice() {
		for (Button btn : diceButtons) {
			releaseStoredDice(btn);
		}
	}

	/**
	 * 
	 * @param interact if true all buttons which are possible to be interacted with
	 *                 will get interactable
	 */
	public void setInteractableButtons(boolean interact, boolean clicked) {

		int id = 0;
		for (int i = 1; i < session.getPlayers().length; i++) {
			if (session.getPlayer(i) != null
					&& session.getPlayer(i).getName().equals(Client.getInstance().getUsername())) {
				id = i;
			}
		}

		if (id == 0) {
			for (InteractableCell ic : iCells1) {
				ic.setInteractable(interact, clicked);
			}
		} else if (id == 1) {
			for (InteractableCell ic : iCells2) {
				ic.setInteractable(interact, clicked);
			}
		} else if (id == 2) {
			for (InteractableCell ic : iCells3) {
				ic.setInteractable(interact, clicked);
			}
		} else {
			for (InteractableCell ic : iCells4) {
				ic.setInteractable(interact, clicked);
			}
		}
	}

	/**
	 * 
	 * @return all dice numbers of 3D-dice or stored ones
	 */
	public ArrayList<Integer> getAllDices() {
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		for (Dice d : diceList) {
			if (d.isVisible()) {
				numbers.add(d.getNumber());
			}
		}
		for (Button btn : diceButtons) {
			if (!btn.getText().equals("-"))
				numbers.add(Integer.parseInt(btn.getText()));
		}
		return numbers;
	}

	/**
	 * 
	 * @return Text of the roll button depending on the current roll
	 */
	private String getButtonLabel() {

		switch (session.getCurrentRoll()) {
		case 1:
			return "1st Roll";
		case 2:
			return "2th Roll";
		case 3:
			return "3rd Roll";
		}
		return "";
	}

	/**
	 * Colors the button of the current player to gray and if the Client is not the
	 * current player, set the color to the current players one
	 */
	public void updateCurrentPlayerButtons() {

		OnlinePlayerWrapper p = session.getCurrentPlayer();

		if (p != null) {

			int id = 0;
			for (int i = 1; i < session.getPlayerCount(); i++) {
				if (session.getPlayer(i).getName().equals(p.getName())) {
					id = i;
				}
			}

			if (p.getName().equals(Client.getInstance().getUsername())) {
				btnRoll.setStyle("");
				btnDice1.setStyle("");
				btnDice2.setStyle("");
				btnDice3.setStyle("");
				btnDice4.setStyle("");
				btnDice5.setStyle("");
			} else {
				String style;

				switch (id) {

				case 1:
					style = "#6e2826";
					break;

				case 2:
					style = "#babe41";
					break;

				case 3:
					style = "#6877ca";
					break;

				default:
					style = "#50be41";
					break;
				}

				style = "-fx-background-color: " + style;
				btnRoll.setStyle(style);
				btnDice1.setStyle(style);
				btnDice2.setStyle(style);
				btnDice3.setStyle(style);
				btnDice4.setStyle(style);
				btnDice5.setStyle(style);
			}
		}
	}

	/**
	 * Updates total score values of current player after updating default scores
	 */
	private void updateTotalLabelsPlayer(OnlinePlayerWrapper p) {

		int id = 0;
		for (int i = 1; i < session.getPlayerCount(); i++) {
			if (session.getPlayer(i).getName().equals(p.getName())) {
				id = i;
			}
		}
		// now id has the value of the current player's id

		ArrayList<InteractableCell> cells;

		switch (id) {

		case 1:
			cells = iCells2;
			break;

		case 2:
			cells = iCells3;
			break;

		case 3:
			cells = iCells4;
			break;

		default:
			cells = iCells1;
			break;
		}

		InteractableCell totalUpper = cells.get(6);
		InteractableCell bonus = cells.get(7);
		InteractableCell totalBonus = cells.get(8);
		InteractableCell totalLower = cells.get(17);
		InteractableCell grandTotal = cells.get(18);
		int[] scores = p.getScores();
		int totalUpperSum = 0;
		for (int i = 0; i < 6; i++) {
			totalUpperSum += scores[i];
		}
		totalUpper.button.setText(totalUpperSum + "");
		if (totalUpperSum > 62) {
			bonus.button.setText("35");
			totalBonus.button.setText((totalUpperSum + 35) + "");
		} else {
			totalBonus.button.setText((totalUpperSum) + "");
		}

		int totalLowerSum = 0;
		for (int i = 6; i < 13; i++) {
			totalLowerSum += scores[i];
		}
		totalLower.button.setText(totalLowerSum + "");
		if (totalUpperSum > 62) {
			grandTotal.button.setText(totalUpperSum + totalLowerSum + 35 + "");
		} else {
			grandTotal.button.setText(totalUpperSum + totalLowerSum + "");
		}
	}

	/**
	 * Gets called when a session ends
	 */
	public void endGame() {
		session = null;
		prevSession = null;
	}

	/**
	 * 
	 * @author KBeck
	 * 
	 *         Embodies all the buttons which can be clicked to select a score
	 *
	 */
	class InteractableCell extends TableCell<String, Void> {
		private final Button button;
		private boolean interactable = false;
		// needed to store the previous number of the button, if not selected
		private int prevNum = 0;
		boolean wasPressed = false;
		private ArrayList<InteractableCell> cells;

		public InteractableCell(ArrayList<InteractableCell> cells) {
			cells.add(this);
			this.cells = cells;
		}

		{
			button = new Button("0");
			button.setStyle("-fx-text-fill: #000000; -fx-background-color: #FFF;");
			button.setMinWidth(86);
			button.setOnAction(ev -> {
				if (interactable) {
					wasPressed = true;
					releaseAllStoredDice();
					for (InteractableCell ic : cells) {
						if (ic != this && ic.interactable) {
							ic.button.setText("0");
						}
					}
					setInteractableButtons(false, true);
					// Sends the server the message that score with id has been chosen
					// 0-5 are aces,twos, etc. Then 10-16 are three of a kind - chance
					int scoreID = cells.indexOf(this);
					// if scoreID 10-16, then substract 4 to get 6-12, thats the format the server
					// uses
					if (scoreID > 9) {
						scoreID -= 4;
					}
					Client.getInstance().selectScore(scoreID);
					// gameSession.nextTurn();
				}
			});
		}

		public boolean isWasPressed() {
			return wasPressed;
		}

		public void setInteractable(boolean interact, boolean clicked) {
			// lock in clicked number
			if (wasPressed) {
				interact = false;
				clicked = false;
			}
			// if is already interactable (or not) do nothing
			if (interactable == interact || isSpecialCell()) {
				return;
			}
			interactable = interact;
			prevNum = Integer.parseInt(button.getText());
			if (interactable) {
				button.setStyle("-fx-text-fill: #000000; -fx-background-color: #FAEF25;");
				button.setMinHeight(30);
				setCellScore(getAllDices());
			} else {
				button.setStyle("-fx-text-fill: #000000; -fx-background-color: #FFF;");
				button.setMinHeight(-1);
				if (!clicked) {
					for (InteractableCell ic : cells) {
						if (!ic.isSpecialCell())
							ic.button.setText(ic.prevNum + "");
					}
				}

			}
		}

		@Override
		protected void updateItem(Void item, boolean empty) {
			super.updateItem(item, empty);
			setGraphic(empty ? null : button);
			// button.setStyle("-fx-background-color: #AAA");
		}

		public void setCellScore(int score) {
			if (!interactable)
				button.setText(score + "");
		}

		public void setCellScore(ArrayList<Integer> allDice) {
			int id = cells.indexOf(this);

			// check if already set
			if (!wasPressed) {

				if (id < 6) {
					int sum = 0;
					for (int i : allDice) {
						if (i == id + 1) {
							sum += (id + 1);
						}
					}
					button.setText(sum + "");
				}
				// three of a kind
				else if (id == 10) {
					button.setText(yh.isThreeOfAKind(allDice) + "");
				}
				// four of a kind
				else if (id == 11) {
					button.setText(yh.isFourOfAKind(allDice) + "");
				} else if (id == 12) {
					button.setText(yh.isFullHouse(allDice) + "");
				} else if (id == 13) {
					button.setText(yh.isLowStraight(allDice) + "");
				} else if (id == 14) {
					button.setText(yh.isHighStraight(allDice) + "");
				} else if (id == 15) {
					button.setText(yh.isYahtzee(allDice) + "");
				}
				// chance
				else if (id == 16) {
					int sum = 0;
					for (int i : allDice) {
						sum += i;
					}
					button.setText(sum + "");
				}
			}
		}

		/**
		 * 
		 * @return true if cell is non-interactable cell
		 */
		private boolean isSpecialCell() {
			int id = cells.indexOf(this);
			return ((id > 5 && id < 10) || id > 16);
		}
	}
}
