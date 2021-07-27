package gameplay;

import java.util.ArrayList;

import application.OfflineGameBoardController;
import application.OnlineGameBoardController;

/**
 * 
 * @author KBeck
 * A 'OfflineSession'-object is used whenever a OfflineGameBoardController is being used.
 */
public class OnlineSession {
	
	//The "human"player is stored in index 0
	private OnlinePlayer players[];
	private int turn,currentRoll;
	private OnlineGameBoardController ogbc;
	public OnlineSession(int botDifficulty, OnlineGameBoardController ogbc) {
		turn = 0;
		currentRoll = 1;
		this.ogbc = ogbc;
	}
	
	public OnlineSession(int botDifficulty, OnlineGameBoardController ogbc, String color) {
		turn = 0;
		currentRoll = 1;
		this.ogbc = ogbc;
	}
	

	
	public int getTurn() {
		return turn;
	}
	
	/**
	 * 
	 * @return either the bot or the player (if turn is %2=0, then return player)
	 */
	public OnlinePlayer getCurrentPlayer() {
		return players[0];
	}
	public int getCurrentRoll() {
		return currentRoll;
	}
	
	public void nextTurn() {
		turn++;
		currentRoll = 1;
		//ogbc.switchCurrentPlayer(getCurrentPlayer() == getPlayer());
		if(turn == 26) {
			//ogbc.endGame();
		}
	}
	
	public OnlinePlayer[] getPlayers() {
		return players;
	}
	
	public void nextRoll() {
		currentRoll++;
		if(currentRoll >3) {
			currentRoll = 1;
		}
	}
}
