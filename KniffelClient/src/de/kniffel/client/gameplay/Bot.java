package de.kniffel.client.gameplay;

import java.util.ArrayList;
import java.util.Random;

import de.kniffel.client.controllers.OfflineGameBoardController;
import de.kniffel.client.utils.YahtzeeHelper;
import javafx.application.Platform;

/**
 * This class includes all bot functionality
 * 
 * @author KBeck
 */
public class Bot extends OfflinePlayer {

	private BotDifficulty difficulty;
	private OfflineGameBoardController ogbc;
	private String color;
	private ArrayList<Integer> alreadySet;
	private YahtzeeHelper yahtzee;

	/**
	 * 
	 * @param difficulty sets the difficulty of the bot (can be one of the
	 *                   following: BotDifficulty.EASY, BotDifficulty.MEDIUM or
	 *                   BotDifficulty.HARD)
	 * @param ogbc       a reference to the Controller, which holds all
	 *                   functionality about an offline game (e.g all scores and
	 *                   dice)
	 */
	public Bot(BotDifficulty difficulty, OfflineGameBoardController ogbc) {
		super("Bot[" + difficulty.name() + "]");
		this.ogbc = ogbc;
		color = "#987267";
		alreadySet = new ArrayList<Integer>();
		this.difficulty = difficulty;
		this.yahtzee = YahtzeeHelper.getInstance();
	}

	/**
	 * @deprecated This constructor is not in use, because it is now only possible
	 *             to play against one Bot with an already set color (red)
	 * @param difficulty sets the difficulty of the bot (can be one of the
	 *                   following: BotDifficulty.EASY, BotDifficulty.MEDIUM or
	 *                   BotDifficulty.HARD)
	 * @param ogbc       a reference to the Controller, which holds all
	 *                   functionality about an offline game (e.g all scores and
	 *                   dice)
	 * @param color      the color of the bot
	 */
	public Bot(BotDifficulty difficulty, OfflineGameBoardController ogbc, String color) {
		super("Bot[" + difficulty.name() + "]");
		this.ogbc = ogbc;
		this.color = color;
		alreadySet = new ArrayList<Integer>();
		this.difficulty = difficulty;
	}

	/**
	 * 
	 * @return difficulty of the bot
	 */
	public BotDifficulty getDifficulty() {
		return difficulty;
	}

	/**
	 * 
	 * @return color of the bot (typically #987267 if the normal constructor was
	 *         used)
	 */
	public String getColor() {
		return color;
	}

	/**
	 * sets the bot's difficulty in hindsight
	 * 
	 * @param difficulty the bot is going to have
	 */
	public void setDifficulty(BotDifficulty difficulty) {
		this.difficulty = difficulty;
	}

	/**
	 * call this to start the next bot round / bot functionality
	 */
	public void start() {
		LogicThread lT = new LogicThread();
		lT.start();
	}

	/**
	 * 
	 * @author KBeck This is used for the bot logic (a thread is useful so that the
	 *         main thread doesnt freeze if we want to add a delay)
	 */
	private class LogicThread extends Thread {

		// Both are used to simulate the behaviour of a real player (e.g random delay
		// time)
		private int delay, delayRatio;

		/**
		 * Default constructor to set "a realistic" delay amount, so that the bot kinda
		 * behaves like a person
		 */
		public LogicThread() {
			this.delay = 1500;
			this.delayRatio = 200; // newDelay() can sleep for a value between 1300 and 1700
		}

		/**
		 * logic is specified in here current function: roll two times and pick the
		 * field with the highest score. This method needs to be extended (function for
		 * easy, medium and hard mode) by @author KevinKloidt
		 */
		@Override
		public void run() {
			// Always choose a random index to set the score to
			if (difficulty == BotDifficulty.EASY) {
				rollDice();
				newDelay();
				Random r = new Random();
				boolean set = false;
				while (set == false) {
					int index = r.nextInt(16);
					// If so there indices are not valid, start again
					if (!(index > 5 && index < 10)) {
						if (!alreadySet.contains(index)) {
							chooseScore(index, ogbc.getScores(),0);
							set = true;
						}
					}
				}
			} else if (difficulty == BotDifficulty.MEDIUM) {
				// Always pick the score with the highest value
				rollDice();
				chooseHighestScore();
			}

			else if (difficulty == BotDifficulty.HARD) {
				// First roll
				rollDice();
				newDelay();
				// Check if Yahtzee, Four of a kind and chooses one of them
				if (checkForYahtzeeFullHouseFourThree(false, false, true)) {
					return;
				}

				// Check three of a kind
				int isThreeOfAKind = yahtzee.getThreeOfAKindNumbers(getAllDices());
				if (isThreeOfAKind != 0) {
					// If the number or four of a kind isnt set yet
					if (!ogbc.isScoreAlreadySet(isThreeOfAKind - 1) || !ogbc.isScoreAlreadySet(11)) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								ogbc.addAllDiceToButtons(isThreeOfAKind);
								// Second roll if the first roll was three of a kind
								rollDice();
								newDelay();
								// if yahtzee, set it
								if (yahtzee.isYahtzee(ogbc.getAllDices()) != 0) {
									// choose yahtzee
									chooseScore(15, ogbc.getScores(),2);
									return;
									// if four of a kind, set numbers first and four of a kind last
								} else if (yahtzee.isFourOfAKind(ogbc.getAllDices()) != 0) {
									if (!ogbc.isScoreAlreadySet(isThreeOfAKind - 1)) {
										// choose the number in the upper section
										chooseScore(isThreeOfAKind - 1, ogbc.getScores(),3);
										return;
									} else {
										// choose four of a kind
										chooseScore(11, ogbc.getScores(),4);
										return;
									}

								}
							}
						});

						// If three of a kind not set, set it
					} else if (!ogbc.isScoreAlreadySet(10)) {
						chooseScore(10, getScores(),5);
						return;
					}
				}

				// Continue, if not yahtzee, four-/three of a kind
				int eyes = getMostEqualDice();

				// If 2-4 dice have the same amount and the corresponding fields are not set
				// yet, add to bank
				if (eyes != 0) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {

							// Add all dice with the corresponding amount to the bank
							ogbc.addAllDiceToButtons(eyes);
						}
					});
					// Second Roll
					rollDice();
					newDelay();
					if (checkForYahtzeeFullHouseFourThree(true, true, true)) {
						return;
					} else {
						newDelay();
						if (!ogbc.getDiceButtons().get(0).getText().equals("-")) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {

									// Add all dice to the bank, which is equal to the first entry in the bank
									ogbc.addAllDiceToButtons(Integer.parseInt(ogbc.getDiceButtons().get(0).getText()));
								}
							});
						}
					}

					// Third roll
					rollDice();
					newDelay();
					if (checkForYahtzeeFullHouseFourThree(true, true, true)) {
						return;
					}

					if (!ogbc.getDiceButtons().get(0).getText().equals("-")) {
						int number = Integer.parseInt(ogbc.getDiceButtons().get(0).getText());
						// if checking for 3,4,5,6
						if (number > 2) {
							// if 3,4,5,6 got rolled at least 3 times
							if (yahtzee.countEquals(ogbc.getAllDices(), number) > 2
									&& !ogbc.isScoreAlreadySet(number - 1)) {
								ogbc.chooseScore(number - 1, getScores());
								return;
							}
						} else {
							// if 1,2 got rolled at least 2 times
							if (!ogbc.isScoreAlreadySet(number - 1)) {
								ogbc.chooseScore(number - 1, getScores());
								return;
							}
						}
					}

					// Choose highest score
					chooseHighestScore();

					// If not 2-4 times the same number was rolled on the first roll
				} else {

					chooseHighestScore();

				}

			}
		}

		/**
		 * always chooses the highest score
		 */
		private void chooseHighestScore() {
			// Choose highest score
			ArrayList<Integer> scores = getScores(); // gets the scores
			// getting max index of scores
			int index = -1;
			int maxValue = 0;
			// set field of highest score
			for (int i = 0; i < scores.size(); i++) {
				if (scores.get(i) > maxValue && !ogbc.isScoreAlreadySet(i) && !ogbc.isScoreAlreadySet(i)
						&& ogbc.isChoosableScoreID(i)) {
					maxValue = scores.get(i);
					index = i;

				}
			}

			// if no highest score field found, set next field
			if (index == -1) {
				for (int i = 0; i < scores.size(); i++) {
					if (ogbc.isChoosableScoreID(i) && !ogbc.isScoreAlreadySet(i)) {
						index = i;
						break;

					}
				}
			}

			chooseScore(index, scores,6); // choose aces

		}

		/**
		 * 
		 * @param fullHouse    if true check for it
		 * @param threeOfAKind if true check for it
		 * @param highStraight if true check for it
		 * @return if one of those were set
		 */
		public boolean checkForYahtzeeFullHouseFourThree(boolean fullHouse, boolean threeOfAKind,
				boolean highStraight) {
			if (yahtzee.isYahtzee(ogbc.getAllDices()) != 0 && !ogbc.isScoreAlreadySet(15)) {
				ogbc.chooseScore(15, getScores());
				return true;
			} else if (fullHouse && yahtzee.isFullHouse(ogbc.getAllDices()) != 0 && !ogbc.isScoreAlreadySet(12)) {
				ogbc.chooseScore(12, getScores());
				return true;
			} else if (yahtzee.isFourOfAKind(ogbc.getAllDices()) != 0 && ogbc.isScoreAlreadySet(11)) {
				ogbc.chooseScore(11, getScores());
				return true;
			} else if (threeOfAKind && yahtzee.isThreeOfAKind(ogbc.getAllDices()) != 0 && ogbc.isScoreAlreadySet(10)) {
				ogbc.chooseScore(10, getScores());
				return true;
			} else if (highStraight && yahtzee.isHighStraight(ogbc.getAllDices()) != 0 && ogbc.isScoreAlreadySet(14)) {
				ogbc.chooseScore(14, getScores());
				return true;
			}
			return false;
		}

		/**
		 * 
		 * @return the number, which got rolled 2-4 times and wasn't set yet (ordered
		 *         from highest to lowest value)
		 */
		public int getMostEqualDice() {

			int[] accumulatedRolls = new int[6];
			newDelay();
			ArrayList<Integer> dice = ogbc.getAllDices();
			for (int i = 0; i < dice.size(); i++) {
				accumulatedRolls[dice.get(i) - 1] = accumulatedRolls[dice.get(i) - 1] + 1;
			}

			int maxValue = 0;
			int eyeAmount = -1;
			for (int i = accumulatedRolls.length - 1; i >= 0; i--) {
				if (accumulatedRolls[i] > maxValue && !ogbc.isScoreAlreadySet(i) && accumulatedRolls[i] > 1
						&& accumulatedRolls[i] < 5) {
					eyeAmount = i;
					maxValue = accumulatedRolls[i];
				}
			}

			int result = eyeAmount + 1;

			if (YahtzeeHelper.getInstance().isFourOfAKind(dice) != 0 && ogbc.isScoreAlreadySet(11)) {
				return 0;
			}
			if (YahtzeeHelper.getInstance().isThreeOfAKind(dice) != 0 && ogbc.isScoreAlreadySet(10)) {
				return 0;
			}

			return result;
		}

		/**
		 * 
		 * @return return the bots scores after a delay
		 */
		public ArrayList<Integer> getScores() {
			newDelay();
			return ogbc.getScores();
		}

		public ArrayList<Integer> getAllDices() {
			newDelay();
			return ogbc.getAllDices();
		}

		/**
		 * 
		 * @param id     field id to set the value to
		 * @param scores array of the bots scores Sets the score with id 'id' after a
		 *               delay
		 */
		public void chooseScore(int id, ArrayList<Integer> scores, int i) {
			newDelay();
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					ogbc.chooseScore(id, scores);
					alreadySet.add(id);
				}
			});
		}

		/**
		 * rerolls the dice after a delay
		 */
		public void rollDice() {
			newDelay();
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					ogbc.rollDice();
				}
			});
		}

		/**
		 * sleeps for a value between delay-delayRatio and delay+delayRatio
		 */
		private void newDelay() {
			Random r = new Random();
			int pos = 1;
			if (r.nextBoolean()) {
				pos = -1;
			}
			try {
				sleep(delay + (r.nextInt(delayRatio + 1) * pos));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @author KBeck a small enumeration to make it easier to understand the
	 *         difficulty of the bot (using an integer to display the difficulty
	 *         would not be as intuitive to understand)
	 *
	 */
	public enum BotDifficulty {
		EASY, MEDIUM, HARD;
	}
}
