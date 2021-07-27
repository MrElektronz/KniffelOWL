package de.kniffel.server;

import java.util.ArrayList;

import de.kniffel.server.GameInstance.GameState;
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
		
		return lobby.getLobbyData();
	}
	
	
	/**
	 * @deprecated
	 * @param sessionID
	 * @return own version of 'null' is an empty array
	 */
	public byte[] updateLobby(String sessionID) {
		System.out.println("Lobby Amount: "+games.size());
		Session s = SessionManager.getInstance().getSession(sessionID);
		if(s != null) {
		System.out.println("update lobby with "+sessionID);
		//add to lobby if not already inside of one
		if(s.getCurrentGame() == null) {
			return addPlayerToLobby(sessionID);
		}
		return s.getCurrentGame().getLobbyData();
		}
		
		return new byte[1];
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
	
	public void setLobbyReadyStatus(String sessionID) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		if(g != null) {
			g.updateReady(sessionID, true);
		}
	}
}
