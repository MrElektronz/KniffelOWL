package de.kniffel.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import de.kniffel.serialize.OnlinePlayerWrapper;
import de.kniffel.serialize.OnlineSessionWrapper;
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
	private Player[] players;
	//a set of all players who clicked ready
	private HashSet<String> ready;
	private SessionManager sm;
	private OnlineSessionWrapper game;
	private int count =1;
	
	public GameInstance() {
		sm = SessionManager.getInstance();
		players = new Player[4];
		state = GameState.Lobby;
		GameFinder.getInstance().games.add(this);
		ready = new HashSet<String>();
		game = new OnlineSessionWrapper(null, null, null, null);
	}
	
	
	public GameState getState() {
		return state;
	}
	

	public Player[] getPlayers() {
		return players;
	}
	
	public Player getCurrentPlayer() {
		for(int i = 0; i< players.length;i++) {
			if(players[i] != null) {
				if(players[i].getUsername().equals(game.getCurrentPlayer().getName())) {
					return players[i];
				}
			}
		}
		return null;
	}
	
	
	public OnlineSessionWrapper getGame() {
		return game;
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
				game.addPlayer(new OnlinePlayerWrapper(players[i].getUsername(), players[i].getProfilePic()));
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
					game.removePlayer(players[i].getUsername());
					players[i] = null;

				}
			}
			}
		Session s = sm.getSession(sessionID);
		if(s != null) {
		s.setCurrentGame(null);
		}
		try {
			if(state == GameState.Lobby) {
			notifyAllClients("!LobbyUpdate;"+Serializer.toString(getLobbyData()));
			}else {
				sendGameUpdate();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * gets called by the current player (client) and sends a game update afterwards
	 */
	public void rollDice(String sessionID) {
		Session s = sm.getSession(sessionID);
		if(game.getCurrentPlayer().getName().equals(s.getUsername())&&game.getCurrentRoll()<4) {
			game.rollDice();
			sendGameUpdate();
		}
	}
	
	/**
	 * gets called by the current player (client) and sends a game update afterwards
	 */
	public void addDiceToBank(String sessionID, int number) {
		Session s = sm.getSession(sessionID);
		if(game.getCurrentPlayer().getName().equals(s.getUsername())&&game.getCurrentRoll()<4) {
			game.getBank().add(number);
			game.getDice().remove((Integer)number);
			sendGameUpdate();
		}
	}
	
	/**
	 * gets called by the current player (client) and sends a game update afterwards
	 */
	public void removeDiceFromBank(String sessionID, int number) {
		Session s = sm.getSession(sessionID);
		if(game.getCurrentPlayer().getName().equals(s.getUsername())&&game.getCurrentRoll()<4) {
			if(game.getBank().contains(number)) {
			game.getBank().remove((Integer)number);
			game.getDice().add(number);
			sendGameUpdate();
			}
		}
	}
	
	
	/**
	 * 
	 * @param fieldID gets selected after dice roll
	 */
	public void selectScore(String sessionID,int fieldID) {
		Session s = sm.getSession(sessionID);
		Player current = getCurrentPlayer();
		if(game.getCurrentPlayer().getName().equals(s.getUsername())&&game.getCurrentRoll()>1 && current!=null) {
			//If not already set, set score
			if(!current.getAlreadySetScores().contains(fieldID)) {
			int score = 1;
			game.selectScore(fieldID,score);
			current.alreadySetScores.add(fieldID);
			sendGameUpdate();
			}
		}
	}
	
	
	/**
	 * sends an updated version of the current game to all connected clients
	 */
	private void sendGameUpdate() {
		notifyAllClients("!GameUpdate;"+game.serialize());
	}
	
	private void sendGameEnd(String winner) {
		notifyAllClients("!GameEnd;"+winner);
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
		if(state == GameState.Lobby) {
		if(bReady) {
			this.ready.add(sessionID);
		}else {
			this.ready.remove(sessionID);
		}
		//check if ready? if so, then start
		getLobbyData();
		
		}
	}
	
	
	private void startGame() {
		if(state == GameState.Lobby) {
			state = GameState.Ingame;
			sendGameUpdate();
		}
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
		if(data[4]==1) {
			startGame();
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
	
	
	
	public void nextTurn() {
		game.setTurn(game.getTurn()+1);
		game.setCurrentRoll(1);
		//ogbc.switchCurrentPlayer(getCurrentPlayer() == getPlayer());
		if(game.getTurn() >= 13*game.getPlayerCount()) {
			//ogbc.endGame();
		}
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
		private String username;
		private ArrayList<Integer> alreadySetScores;
		public Player(String sessionID) {
			this.sessionID = sessionID;
			username = "";
			alreadySetScores = new ArrayList<>();
		}
		
		public Session getSession() {
			return sm.getSession(sessionID);
		}
		
		public ArrayList<Integer> getAlreadySetScores() {
			return alreadySetScores;
		}
		
		public String getUsername() {
			if(username.equals("")) {
			Session s = sm.getSession(sessionID);
			if(s != null) {
			this.username = s.getUsername();
			}else {
				this.username = "";
			}
			}
			return username;
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
