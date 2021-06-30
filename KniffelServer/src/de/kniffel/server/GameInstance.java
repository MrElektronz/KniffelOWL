package de.kniffel.server;

import java.util.HashMap;

import de.kniffel.server.SessionManager.Session;

/**
 * This class is an instance of a game, it contains all the players playing the game and other important information about it
 * @author KBeck
 *
 */
public class GameInstance {
	
	private GameState state;
	private Player currentPlayer;
	private Player[] players;
	
	public GameInstance() {
		players = new Player[4];
		currentPlayer = null;
		state = GameState.Lobby;
		GameFinder.games.add(this);
	}
	
	
	public GameState getState() {
		return state;
	}
	
	public Player getCurrentPlayer() {
		return currentPlayer;
	}
	public Player[] getPlayers() {
		return players;
	}
	
	/**
	 * 
	 * @return true if successful
	 */
	public boolean addPlayer(String sessionID) {
		if(state == GameState.Ingame) {
			return false;
		}
		for(int i = 0; i<players.length;i++) {
			if(players[i] == null) {
				players[i] = new Player(sessionID);
				SessionManager.getSession(sessionID).setCurrentGame(this);
				
				return true;
			}
		}
		return false;
	}
	
	/**
	 *  removes the player from the game
	 * @param sessionID
	 */
	public void removePlayer(String sessionID) {
		for(int i = 0; i<players.length;i++) {
			if(players[i] != null) {
				if(players[i].getSessionID().equals(sessionID)) {
					players[i] = null;
				}
			}
			}
		Session s = SessionManager.getSession(sessionID);
		s.setCurrentGame(null);
	}
	
	/**
	 * 
	 * @param sessionID
	 * @return true if player is in lobby, false if not
	 */
	public boolean containsPlayer(String sessionID) {
		for(int i = 0; i<players.length;i++) {
			if(players[i] != null && players[i].getSessionID().equals(sessionID)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return null if ingame, otherwise index 0-3 are player profile pictures (-1 if no player) and index 4 is if ready (1 for true)
	 */
	public int[] getLobbyData() {
		if(state == GameState.Ingame) {
			return new int[1];
		}
		int[] data = new int[5];
		//needs to be changed to 1 later
		data[4] = 0;
		for(int i = 0; i<players.length;i++) {
			if(players[i] != null) {
				//add profilepicID to array
				data[i] = players[i].getProfilePic();
			}
		}
		return data;
	}
	
	/**
	 * 
	 * @return true if gamestate is either ingame or it is lobby, but there are already 4 people joined
	 */
	public boolean isFull() {
		if(state == GameState.Ingame) {
			return true;
		}
		
		for(int i = 0; i<players.length;i++) {
			if(players[i] == null) {
				return false;
			}
		}
		return true;
	}
	
	public int getPlayerCount() {
		int x = 0;
		for(int i = 0; i<players.length;i++) {
			if(players[i] != null) {
				x++;
			}
		}
		return x;
	}
	
	
	class Player{
		private String sessionID;
		private HashMap<Integer, Integer> score;
		public Player(String sessionID) {
			this.sessionID = sessionID;
			score = new HashMap<Integer, Integer>();
		}
		
		public Session getSession() {
			return SessionManager.getSession(sessionID);
		}
		
		public String getUsername() {
			Session s = SessionManager.getSession(sessionID);
			if(s != null) {
			return s.getUsername();
			}else {
				return "";
			}
		}
		
		public HashMap<Integer, Integer> getScore() {
			return score;
		}
		public void setScore(HashMap<Integer, Integer> score) {
			this.score = score;
		}
		
		public int getProfilePic() {
			return Server.getSQLManager().getProfilePic(getUsername());
		}
		
		public String getSessionID() {
			return sessionID;
		}
	}
	
	enum GameState{
		Lobby,Ingame;
	}
	
}
