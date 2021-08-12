package de.kniffel.server.main;

import java.util.ArrayList;
import java.util.Random;

import de.kniffel.server.main.GameInstance.GameState;
import de.kniffel.server.main.GameInstance.Player;
import de.kniffel.server.utils.SessionManager.Session;


/**
 * Maybe TODO: create LobbyInstance as parent class of GameInstance (gameinstance has more functionality) <- Decided against it
 * @author KBeck
 *
 */
public class GameFinder {
	
	/*Warum wir keine neue Arraylist f�r lobbys genutzt haben: Wollen den Umfang des Server so simpel wie m�glich halten, daher auf unn�tige Strukturen
	 * verzichten. W�re wohl etwas effizienter, da wir durch unsere Anforderungen aber nicht hunderte Spiele gleichzeitig laufen haben wollen,
	 * wird sich diese Herangehensweise (�ber alle games loopen) nicht als wesentlich langsamer darstellen. 
	 */

	private static GameFinder instance;
	public ArrayList<GameInstance> games = new ArrayList<GameInstance>();
	
	/**
	 * private constructor, so no other class can instanciate GameFinder
	 */
	private GameFinder() {
		
	}
	
	/**
	 * using the singleton design pattern
	 * @return one and only instance of this class
	 */
	public static GameFinder getInstance() {
		if(instance == null) {
			instance = new GameFinder();
		}
		return instance;
	}
	
	
	/**
	 * 
	 * @param sessionID Adds player with this sessionID to a lobby
	 */
	public void addPlayerToLobby(String sessionID) {
		GameInstance lobby = null;
		for(GameInstance game : games) {
			if(game.getState() == GameState.Lobby) {
				lobby = game;
			}
		}
		//no active lobby, create a new one
		if(lobby == null) {
			lobby = new GameInstance();
		}
		//add Player to lobby
		lobby.addPlayer(sessionID);
		
		System.out.println("Added session "+sessionID+" to lobby");
		
	}
	
	
	
	/**
	 * 
	 * @param sessionID Removes player with this sessionID from the lobby
	 */
	public void removePlayerFromLobby(String sessionID) {
		GameInstance toDelete = null;
		for(GameInstance game : games) {
			if(game.containsPlayer(sessionID)) {
				game.removePlayer(sessionID);
				toDelete = game;
			}
		}
		if(toDelete != null && toDelete.getPlayerCount()==0) {
			games.remove(toDelete);
		}
	}
	
	/**
	 * 
	 * @param sessionID
	 * @return The GameInstance the player is playing in. Returns null when not in a game
	 */
	public GameInstance getGameInstanceOfPlayer(String sessionID) {
		GameInstance g = null;
		for(GameInstance game : games) {
			if(game.containsPlayer(sessionID)) {
				g = game;
			}
		}
		return g;
	}
	
	/**
	 * 
	 * @param sessionID
	 * @return byte[] length of 0 if it's not sessionID's turn 
	 */
	public void rollDice(String sessionID) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		g.rollDice(sessionID);
	}
	
	/**
	 * Adds dice to bank of player's GameInstance
	 * @param sessionID
	 * @param number
	 */
	public void addDiceToBank(String sessionID, int number) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		g.addDiceToBank(sessionID, number);
	}
	
	/**
	 * Remove dice from bank of player's GameInstance
	 * @param sessionID
	 * @param number
	 */
	public void removeDiceFromBank(String sessionID, int number) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		g.removeDiceFromBank(sessionID, number);
	}
	
	/**
	 * Sets score of player's GameInstance
	 * @param sessionID
	 * @param scoreID
	 */
	public void setScore(String sessionID, int scoreID) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		g.selectScore(sessionID, scoreID);
	}
	
	/**
	 * 
	 * @param sessionID is set to ready in its lobby
	 */
	public void setLobbyReadyStatus(String sessionID) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		if(g != null) {
			g.updateReady(sessionID, true);
		}
	}
}