package de.kniffel.client.gameplay;

import de.kniffel.client.controllers.OfflineGameBoardController;
import de.kniffel.client.gameplay.Bot.BotDifficulty;

/**
 * A "OfflineSession"-Object is used whenever an OfflineGameBoardController is
 * being used. This helps keeping the Controller more organized.
 * 
 * @author KBeck
 */
public class OfflineSession {

	// The "human"player is stored in index 0
	private OfflinePlayer player;
	private Bot bot;
	private int turn, currentRoll;
	private OfflineGameBoardController ogbc;

	/**
	 * 
	 * @param botDifficulty sets the difficulty of the bot (can be one of the
	 *                      following: BotDifficulty.EASY, BotDifficulty.MEDIUM or
	 *                      BotDifficulty.HARD)
	 * @param ogbc          a reference to the Controller, which holds all
	 *                      functionality about an offline game (e.g all scores and
	 *                      dice)
	 */
	public OfflineSession(BotDifficulty botDifficulty, OfflineGameBoardController ogbc) {
		turn = 0;
		currentRoll = 1;
		bot = new Bot(botDifficulty, ogbc);
		this.ogbc = ogbc;
	}

	/**
	 * @deprecated This constructor is not in use, because it is now only possible
	 *             to play against one Bot with an already set color (red)
	 * @param botDifficulty sets the difficulty of the bot (can be one of the
	 *                      following: BotDifficulty.EASY, BotDifficulty.MEDIUM or
	 *                      BotDifficulty.HARD)
	 * @param ogbc          a reference to the Controller, which holds all
	 *                      functionality about an offline game (e.g all scores and
	 *                      dice)
	 * @param color         the color of the bot
	 */
	public OfflineSession(BotDifficulty botDifficulty, OfflineGameBoardController ogbc, String color) {
		turn = 0;
		currentRoll = 1;
		bot = new Bot(botDifficulty, ogbc, color);
		this.ogbc = ogbc;
	}

	/**
	 * sets the player object
	 * 
	 * @param op OfflinePlayer instance
	 */
	public void setPlayer(OfflinePlayer op) {
		player = op;
	}

	/**
	 * 
	 * @return current turn
	 */
	public int getTurn() {
		return turn;
	}

	/**
	 * 
	 * @return either the bot or the player (if turn is %2=0, then return player)
	 */
	public OfflinePlayer getCurrentPlayer() {
		return (turn % 2 == 0) ? player : bot;
	}

	/**
	 * 
	 * @return current roll
	 */
	public int getCurrentRoll() {
		return currentRoll;
	}

	/**
	 * gets called to start the next turn
	 */
	public void nextTurn() {
		turn++;
		currentRoll = 1;
		ogbc.switchCurrentPlayer(getCurrentPlayer() == getPlayer());
		if (turn == 26) {
			ogbc.endGame();
		}
	}

	/**
	 * 
	 * @return the player object
	 */
	public OfflinePlayer getPlayer() {
		return player;
	}

	/**
	 * 
	 * @return the bot object
	 */
	public Bot getBot() {
		return bot;
	}

	/**
	 * increments the current roll (or changes it to 1 if it exceeds 3)
	 */
	public void nextRoll() {
		currentRoll++;
		if (currentRoll > 3) {
			currentRoll = 1;
		}
	}
}
