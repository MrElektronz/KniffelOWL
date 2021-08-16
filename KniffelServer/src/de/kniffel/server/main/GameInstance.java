package de.kniffel.server.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import de.kniffel.server.serialize.OnlinePlayerWrapper;
import de.kniffel.server.serialize.OnlineSessionWrapper;
import de.kniffel.server.serialize.Serializer;
import de.kniffel.server.threads.ServerRequestWorkerThread;
import de.kniffel.server.utils.SessionManager;
import de.kniffel.server.utils.SessionManager.Session;
import de.kniffel.server.utils.YahtzeeHelper;

/**
 * This class is an instance of a game, it contains all the players playing the
 * game and other important information about it
 * 
 * @author KBeck
 *
 */
public class GameInstance {

	private GameState state;
	private Player[] players;

	// a set of all players who clicked ready
	private HashSet<String> ready;

	private SessionManager sm;
	private OnlineSessionWrapper game;

	/**
	 * Generates a new, empty game instance, which starts as a lobby
	 */
	public GameInstance() {
		sm = SessionManager.getInstance();
		players = new Player[4];
		state = GameState.Lobby;
		GameFinder.getInstance().addGameInstance(this);
		ready = new HashSet<String>();
		game = new OnlineSessionWrapper(null, null, null, null);
	}

	/**
	 * 
	 * @return state of the game instance (either GameState.Lobby or
	 *         GameState.Ingame)
	 */
	public GameState getState() {
		return state;
	}

	/**
	 * 
	 * @return Array of all players, can include "null"
	 */
	public Player[] getPlayers() {
		return players;
	}

	/**
	 * 
	 * @return the player whose turn it is
	 */
	public Player getCurrentPlayer() {
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				if (players[i].getUsername().equals(game.getCurrentPlayer().getName())) {
					return players[i];
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @return the sessionWrapper, this object includes the current turn, scores and
	 *         more
	 */
	public OnlineSessionWrapper getGame() {
		return game;
	}

	/**
	 * @param sessionID of the user
	 * @return true if successful
	 */
	public boolean addPlayer(String sessionID) {
		if (state == GameState.Ingame) {
			return false;
		}
		for (int i = 0; i < players.length; i++) {
			if (players[i] == null) {
				players[i] = new Player(sessionID);
				game.addPlayer(new OnlinePlayerWrapper(players[i].getUsername(), players[i].getProfilePic()));
				sm.getSession(sessionID).setCurrentGame(this);

				// Sending new lobby status to all clients, because Base64 doesn't include ';',
				// we can use it as a separator
				try {
					notifyAllClients("!LobbyUpdate;" + Serializer.toString(getLobbyData()));
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
	 * CAUTION: This method does not delete the game instance when it's empty,
	 * please use GameFinder.getInstance().removePlayerFromLobby(sessionID) for
	 * this. Removes the player from the game
	 * 
	 * @param sessionID of the user
	 */
	public void removePlayer(String sessionID) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				if (players[i].getSessionID().equals(sessionID)) {
					updateReady(sessionID, false);
					game.removePlayer(players[i].getUsername());
					players[i] = null;

				}
			}
		}
		Session s = sm.getSession(sessionID);
		if (s != null) {
			s.setCurrentGame(null);
		}
		try {
			if (state == GameState.Lobby) {
				notifyAllClients("!LobbyUpdate;" + Serializer.toString(getLobbyData()));
			} else {
				sendGameUpdate();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param sessionID
	 * 
	 *                  gets called by the current player (client) and sends a game
	 *                  update afterwards
	 */
	public void rollDice(String sessionID) {
		Session s = sm.getSession(sessionID);
		if (game != null && s != null && game.getCurrentPlayer().getName().equals(s.getUsername())
				&& game.getCurrentRoll() < 4) {
			game.rollDice();
			sendGameUpdate();
		}
	}

	/**
	 * 
	 * @param sessionID of the user
	 * @param number the number the dice shows, which gets added to the bank
	 * 
	 *                  gets called by the current player (client) and sends a game
	 *                  update afterwards
	 */
	public void addDiceToBank(String sessionID, int number) {
		Session s = sm.getSession(sessionID);
		if (game.getCurrentPlayer().getName().equals(s.getUsername()) && game.getCurrentRoll() < 4
				&& game.getCurrentRoll() > 1) {
			game.getBank().add(number);
			game.getDice().remove((Integer) number);
			sendGameUpdate();
		}
	}

	/**
	 * 
	 * @param sessionID of the user
	 * @param number the number the dice shows, which gets removed to the bank
	 * 
	 *                  gets called by the current player (client) and sends a game
	 *                  update afterwards
	 */
	public void removeDiceFromBank(String sessionID, int number) {
		Session s = sm.getSession(sessionID);
		if (game.getCurrentPlayer().getName().equals(s.getUsername())) {
			if (game.getBank().contains(number)) {
				game.getBank().remove((Integer) number);
				game.getDice().add(number);
				sendGameUpdate();
			}
		}
	}

	
	/**
	 * 
	 * @param id gets selected after dice roll
	 * @param sessionID of the user
	 */
	public void selectScore(String sessionID, int id) {
		Session s = sm.getSession(sessionID);
		Player current = getCurrentPlayer();
		if (game.getCurrentPlayer().getName().equals(s.getUsername()) && current != null) {

			// If not already set, set score
			if (!current.getAlreadySetScores().contains(id)) {
				int score = 1;

				if (id < 6) {
					int sum = 0;
					for (int i : game.getCombinedDice()) {
						if (i == id + 1) {
							sum += (id + 1);
						}
					}
					score = sum;
				} else if (id == 6) {
					score = YahtzeeHelper.getInstance().isThreeOfAKind(game.getCombinedDice());
				}
				// four of a kind
				else if (id == 7) {
					score = YahtzeeHelper.getInstance().isFourOfAKind(game.getCombinedDice());
				} else if (id == 8) {
					score = YahtzeeHelper.getInstance().isFullHouse(game.getCombinedDice());
				} else if (id == 9) {
					score = YahtzeeHelper.getInstance().isLowStraight(game.getCombinedDice());
				} else if (id == 10) {
					score = YahtzeeHelper.getInstance().isHighStraight(game.getCombinedDice());
				} else if (id == 11) {
					score = YahtzeeHelper.getInstance().isYahtzee(game.getCombinedDice());
				}
				// chance
				else if (id == 12) {
					int sum = 0;
					for (int i : game.getCombinedDice()) {
						sum += i;
					}
					score = sum;
				}
				game.selectScore(id, score);
				current.alreadySetScores.add(id);
				sendGameUpdate();
			}

			// End the game when every score has been set (if the player is not null)
			boolean end = true;
			for (Player all : players) {
				if (all != null) {
					if (all.getAlreadySetScores().size() < 13) {
						end = false;
					}
				}
			}

			if (end) {

				// determine winner of the game
				int best = 0;
				OnlinePlayerWrapper winner = null;

				for (OnlinePlayerWrapper all : game.getPlayers()) {
					if (all != null) {
						int grandTotal = 0;

						int[] scores = all.getScores();
						int totalUpperSum = 0;
						for (int i = 0; i < 6; i++) {
							totalUpperSum += scores[i];
						}

						int totalLowerSum = 0;
						for (int i = 6; i < 13; i++) {
							totalLowerSum += scores[i];
						}
						if (totalUpperSum > 62) {
							grandTotal = 35 + totalLowerSum + totalUpperSum;
						} else {
							grandTotal = totalLowerSum + totalUpperSum;
						}

						if (grandTotal > best) {
							best = grandTotal;
							winner = all;
						}
					}
				}

				// End the game
				sendGameEnd(winner);

			}

		}
	}

	/**
	 * sends an updated version of the current game to all connected clients
	 */
	private void sendGameUpdate() {
		notifyAllClients("!GameUpdate;" + game.serialize());
	}

	/**
	 * Ends the game
	 * 
	 * @param winner the winner player
	 */
	private void sendGameEnd(OnlinePlayerWrapper winner) {
		if (winner != null) {
			notifyAllClients("!GameEnd;" + winner.getName());
		} else {
			notifyAllClients("!GameEnd;L");
		}
		for (Player all : players) {
			// Use this method so the game instance gets deleted afterwards
			if (all != null) {
				GameFinder.getInstance().removePlayerFromLobby(all.getSessionID());

				ServerMain.getSQLManager().addPlayedGame(all.getUsername());

				if (all.getUsername().equals(winner.getName())) {
					ServerMain.getSQLManager().addWin(all.getUsername());
					ServerMain.getSQLManager().addPoints(all.getUsername(), 20);
				} else {
					ServerMain.getSQLManager().addLoss(all.getUsername());
					ServerMain.getSQLManager().addPoints(all.getUsername(), 5);
				}

			}
		}
	}

	/**
	 * 
	 * @param sessionID of the user
	 * @return true if player is in lobby, false if not
	 */
	public boolean containsPlayer(String sessionID) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null && players[i].getSessionID().equals(sessionID)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets called whenever a client presses the "Ready"-button in the lobby
	 * 
	 * @param sessionID of the user
	 * @param bReady if user is ready or not
	 */
	public void updateReady(String sessionID, boolean bReady) {
		if (state == GameState.Lobby) {
			if (bReady) {
				this.ready.add(sessionID);
			} else {
				this.ready.remove(sessionID);
			}
			// check if ready? if so, then start
			getLobbyData();

		}
	}

	/**
	 * starts the game if the game instance is a lobby
	 */
	private void startGame() {
		if (state == GameState.Lobby) {
			state = GameState.Ingame;
			sendGameUpdate();
		}
	}

	/**
	 * Lobby is ready if every player clicks ready (min 2 players) or 4 players are
	 * in the lobby
	 * 
	 * @return null if ingame, otherwise index 0-3 are player profile pictures (-1
	 *         if no player) and index 4 is if ready (1 for true)
	 */
	public byte[] getLobbyData() {
		if (state == GameState.Ingame) {
			return new byte[1];
		}
		byte[] data = new byte[5];
		// needs to be changed to 1 later
		data[4] = 0;
		int playerCount = getPlayerCount();
		if ((playerCount > 3 || ready.size() >= playerCount) && playerCount > 1) {
			data[4] = 1;
		}
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				// add profilepicID to array
				data[i] = players[i].getProfilePic();
			}
		}
		if (data[4] == 1) {
			startGame();
		}
		return data;
	}

	/**
	 * 
	 * @return true if gamestate is either ingame or it is lobby, but there are
	 *         already 4 people joined
	 */
	public boolean isFull() {
		if (state == GameState.Ingame) {
			return true;
		}

		for (int i = 0; i < players.length; i++) {
			if (players[i] == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @return amount of all connected players in this game instance
	 */
	public int getPlayerCount() {
		int x = 0;
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				x++;
			}
		}
		return x;
	}

	/**
	 * sends data to every client in the game instance
	 * 
	 * @param data to be send to all connected clients
	 */
	public void notifyAllClients(String data) {
		for (int i = 0; i < players.length; i++) {
			if (players[i] != null) {
				try {
					ServerRequestWorkerThread wThread = sm.getSession(players[i].getSessionID()).getWorkerThread();
					if (wThread.getClient().isConnected()) {
						wThread.getOut().writeUTF(data);
						wThread.getOut().flush();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				;
			}
		}
	}

	/**
	 * A trivial class which helps handling data about each player in the game
	 * instance
	 * 
	 * @author KBeck
	 *
	 */
	class Player {
		private String sessionID;
		private String username;
		private ArrayList<Integer> alreadySetScores;

		public Player(String sessionID) {
			this.sessionID = sessionID;
			username = "";
			alreadySetScores = new ArrayList<>();
		}

		/**
		 * 
		 * @return player's session
		 */
		public Session getSession() {
			return sm.getSession(sessionID);
		}

		/**
		 * 
		 * @return ArrayList of all score fields which have already a score value
		 *         assigned to it
		 */
		public ArrayList<Integer> getAlreadySetScores() {
			return alreadySetScores;
		}

		/**
		 * 
		 * @return player's username
		 */
		public String getUsername() {
			if (username.equals("")) {
				Session s = sm.getSession(sessionID);
				if (s != null) {
					this.username = s.getUsername();
				} else {
					this.username = "";
				}
			}
			return username;
		}

		/**
		 * 
		 * @return player's profile pic ID
		 */
		public byte getProfilePic() {
			return ServerMain.getSQLManager().getProfilePic(getUsername());
		}

		/**
		 * 
		 * @return player's sessionID
		 */
		public String getSessionID() {
			return sessionID;
		}
	}

	/**
	 * 
	 * @author KBeck
	 * 
	 *         a small enumeration to make it more intuitive to determine whether a
	 *         game instance is still of type "Lobby" or already "Ingame"
	 *
	 */
	enum GameState {
		Lobby, Ingame;
	}

}
