package de.kniffel.server;

import java.util.ArrayList;
import java.util.Random;

import de.kniffel.server.GameInstance.GameState;
import de.kniffel.server.GameInstance.Player;
import de.kniffel.server.SessionManager.Session;


/**
 * Maybe todo: create LobbyInstance as parent class of GameInstance (gameinstance has more functionality)
 * @author KBeck
 *
 */
public class GameFinder {
	
	/*Warum wir keine neue Arraylist für lobbys genutzt haben: Wollen den Umfang des Server so simpel wie möglich halten, daher auf unnötige Strukturen
	 * verzichten. Wäre wohl etwas effizienter, da wir durch unsere Anforderungen aber nicht hunderte Spiele gleichzeitig laufen haben wollen,
	 * wird sich diese Herangehensweise (über alle games loopen) nicht als wesentlich langsamer darstellen. 
	 */

	private static GameFinder instance;
	
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
	
	public ArrayList<GameInstance> games = new ArrayList<GameInstance>();
	
	public byte[] addPlayerToLobby(String sessionID) {
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
		
		return lobby.getLobbyData();
	}
	
	
	
	
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
	
	public void addDiceToBank(String sessionID, int number) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		g.addDiceToBank(sessionID, number);
	}
	
	public void removeDiceFromBank(String sessionID, int number) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		g.removeDiceFromBank(sessionID, number);
	}
	
	public void setScore(String sessionID, int scoreID) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		g.selectScore(sessionID, scoreID);
	}
	
	public void setLobbyReadyStatus(String sessionID) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		if(g != null) {
			g.updateReady(sessionID, true);
		}
		System.out.println("Ready set go?");
	}
}
