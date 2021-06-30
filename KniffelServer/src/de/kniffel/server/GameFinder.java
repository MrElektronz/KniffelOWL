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

	public static ArrayList<GameInstance> games = new ArrayList<GameInstance>();
	
	private static int[] addPlayerToLobby(String sessionID) {
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
	 * 
	 * @param sessionID
	 * @return own version of 'null' is an empty array
	 */
	public static int[] updateLobby(String sessionID) {
		System.out.println("Lobby Amount: "+games.size());
		Session s = SessionManager.getSession(sessionID);
		if(s != null) {
		System.out.println("update lobby with "+sessionID);
		//add to lobby if not already inside of one
		if(s.getCurrentGame() == null) {
			return addPlayerToLobby(sessionID);
		}
		return s.getCurrentGame().getLobbyData();
		}
		
		return new int[1];
	}
	
	
	public static void removePlayerFromLobby(String sessionID) {
		GameInstance toDelete = null;
		for(GameInstance game : games) {
			if(game.containsPlayer(sessionID)) {
				game.removePlayer(sessionID);
				toDelete = game;
			}
		}
		if(toDelete != null && toDelete.getPlayerCount()==0) {
			GameFinder.games.remove(toDelete);
		}
	}
}
