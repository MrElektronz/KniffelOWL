package gameplay;

import java.util.ArrayList;

import application.OfflineGameBoardController;
import gameplay.Bot.BotDifficulty;

/**
 * 
 * @author KBeck
 * A 'OfflineSession'-object is used whenever a OfflineGameBoardController is being used.
 */
public class OfflineSession {
	
	//The "human"player is stored in index 0
	private OfflinePlayer player;
	private Bot bot;
	private int turn,currentRoll;
	private OfflineGameBoardController ogbc;
	public OfflineSession(BotDifficulty botDifficulty, OfflineGameBoardController ogbc) {
		turn = 0;
		currentRoll = 1;
		bot = new Bot(botDifficulty,ogbc);
		this.ogbc = ogbc;
	}
	
	public OfflineSession(BotDifficulty botDifficulty, OfflineGameBoardController ogbc, String color) {
		turn = 0;
		currentRoll = 1;
		bot = new Bot(botDifficulty,ogbc,color);
		this.ogbc = ogbc;
	}
	
	public void setPlayer(OfflinePlayer op)
	{
		player = op;
	}
	
	
	public int getTurn() {
		return turn;
	}
	
	/**
	 * 
	 * @return either the bot or the player (if turn is %2=0, then return player)
	 */
	public Player getCurrentPlayer() {
		return (turn%2 == 0)?player:bot;
	}
	public int getCurrentRoll() {
		return currentRoll;
	}
	
	public void nextTurn() {
		turn++;
		currentRoll = 1;
		ogbc.switchCurrentPlayer(getCurrentPlayer() == getPlayer());
		if(turn == 26) {
			ogbc.endGame();
		}
	}
	
	public OfflinePlayer getPlayer() {
		return player;
	}
	public Bot getBot() {
		return bot;
	}
	
	public void nextRoll() {
		currentRoll++;
		if(currentRoll >3) {
			currentRoll = 1;
		}
	}
}
