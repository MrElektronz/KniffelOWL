package de.kniffel.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import de.kniffel.serialize.Serializer;
import de.kniffel.server.SessionManager.Session;
import de.kniffel.server.threads.ServerRequestWorkerThread;

/**
 * This class is an instance of a game, it contains all the players playing the game and other important information about it
 * @author KBeck
 *
 */
public class GameInstance {
	
	private GameState state;
	private Player currentPlayer;
	private Player[] players;
	//a set of all players who clicked ready
	private HashSet<String> ready;
	private SessionManager sm;
	
	public GameInstance() {
		sm = SessionManager.getInstance();
		players = new Player[4];
		currentPlayer = null;
		state = GameState.Lobby;
		GameFinder.getInstance().games.add(this);
		ready = new HashSet<String>();
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
				sm.getSession(sessionID).setCurrentGame(this);
				
				//Sending new lobby status to all clients, because Base64 doesn't include ';', we can use it as a separator
				try {
					notifyAllClients("!LobbyUpdate;"+Serializer.toString(getLobbyData()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
					updateReady(sessionID,false);
					players[i] = null;
				}
			}
			}
		Session s = sm.getSession(sessionID);
		s.setCurrentGame(null);
		
		try {
			notifyAllClients("!LobbyUpdate;"+Serializer.toString(getLobbyData()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	
	public void updateReady(String sessionID, boolean bReady) {
		if(bReady) {
			this.ready.add(sessionID);
		}else {
			this.ready.remove(sessionID);
		}
		//TODO: remove
		notifyAllClients("!test");
	}
	
	
	/**
	 * Lobby is ready if every player clicks ready (min 2 players) or 4 players are in the lobby
	 * @return null if ingame, otherwise index 0-3 are player profile pictures (-1 if no player) and index 4 is if ready (1 for true)
	 */
	public byte[] getLobbyData() {
		if(state == GameState.Ingame) {
			return new byte[1];
		}
		byte[] data = new byte[5];
		//needs to be changed to 1 later
		data[4] = 0;
		int playerCount = getPlayerCount();
		if(playerCount>3||ready.size()>=playerCount) {
			data[4] = 1;
		}
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
	

	/**
	 * sends data to every client in the game instance
	 * @param data
	 */
	public void notifyAllClients(String data) {
		for(int i = 0; i< players.length;i++) {
			if(players[i] != null) {
				try {
					System.out.println("SENDING NOTIFY TO: "+players[i].getUsername());
					ServerRequestWorkerThread wThread = sm.getSession(players[i].getSessionID()).getWorkerThread();
					if(wThread.getClient().isConnected()) {
					wThread.getOut().writeUTF(data);
					wThread.getOut().flush();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
		}
	}
	
	
	class Player{
		private String sessionID;
		private HashMap<Integer, Integer> score;
		public Player(String sessionID) {
			this.sessionID = sessionID;
			score = new HashMap<Integer, Integer>();
		}
		
		public Session getSession() {
			return sm.getSession(sessionID);
		}
		
		public String getUsername() {
			Session s = sm.getSession(sessionID);
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
		
		public byte getProfilePic() {
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
