package de.kniffel.client.gameplay;

import java.util.ArrayList;
import java.util.Random;

import de.kniffel.client.controllers.OfflineGameBoardController;
import javafx.application.Platform;

/**
 * 
 * @author KBeck This class includes all bot functionality
 */
public class Bot extends OfflinePlayer {

	private BotDifficulty difficulty;
	private OfflineGameBoardController ogbc;
	private String color;
	private ArrayList<Integer> alreadySet;

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
	 * @param difficulty
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
			// Sample code, roll 2 times, then add to the field with highest score
			rollDice();
			rollDice();
			ArrayList<Integer> scores = getScores(); // gets the scores
			// getting max index of scores
			int index = -1;
			int maxValue = 0;
			// set field of highest score
			for (int i = 0; i < scores.size(); i++) {
				if (scores.get(i) > maxValue && !ogbc.isScoreAlreadySet(i) && !alreadySet.contains(i)
						&& ogbc.isChoosableScoreID(i)) {
					maxValue = scores.get(i);
					index = i;
				}
			}

			// if no highest score field found, set next field
			if (index == -1) {
				for (int i = 0; i < scores.size(); i++) {
					if (ogbc.isChoosableScoreID(i) && !alreadySet.contains(i)) {
						index = i;
						break;
					}
				}
			}

			chooseScore(index, scores); // choose aces

		}

		/**
		 * 
		 * @return return the bots scores after a delay
		 */
		public ArrayList<Integer> getScores() {
			newDelay();
			return ogbc.getScores();
		}

		/**
		 * 
		 * @param id     field id to set the value to
		 * @param scores array of the bots scores Sets the score with id 'id' after a
		 *               delay
		 */
		public void chooseScore(int id, ArrayList<Integer> scores) {
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
