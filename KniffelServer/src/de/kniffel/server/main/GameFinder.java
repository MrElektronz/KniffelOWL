package de.kniffel.server.main;

import java.util.ArrayList;

import de.kniffel.server.main.GameInstance.GameState;

/**
 * This class handles all game instances.
 * 
 * Maybe TODO: create LobbyInstance as parent class of GameInstance
 * (gameinstance has more functionality). Decided against it, because it works
 * just fine the way it is now.
 * 
 * @author KBeck
 *
 */
public class GameFinder {

	/*
	 * Warum wir keine neue Arraylist für lobbys genutzt haben: Wollen den Umfang
	 * des Server so simpel wie möglich halten, daher auf unnötige Strukturen
	 * verzichten. Wäre wohl etwas effizienter, da wir durch unsere Anforderungen
	 * aber nicht hunderte Spiele gleichzeitig laufen haben wollen, wird sich diese
	 * Herangehensweise (über alle games loopen) nicht als wesentlich langsamer
	 * darstellen.
	 */

	private static GameFinder instance;
	private ArrayList<GameInstance> games = new ArrayList<GameInstance>();

	/**
	 * private constructor, so no other class can instanciate GameFinder
	 */
	private GameFinder() {
	}

	/**
	 * using the singleton design pattern
	 * 
	 * @return one and only instance of this class
	 */
	public static GameFinder getInstance() {
		if (instance == null) {
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

		// Search for a lobby and select it
		for (GameInstance game : games) {
			if (game.getState() == GameState.Lobby) {
				lobby = game;
			}
		}

		// if no active lobby found, create a new one
		if (lobby == null) {
			lobby = new GameInstance();
		}

		// add Player to lobby
		lobby.addPlayer(sessionID);

		System.out.println("Added session " + sessionID + " to lobby");

	}

	/**
	 * 
	 * @param sessionID Removes player with this sessionID from the lobby
	 */
	public void removePlayerFromLobby(String sessionID) {
		GameInstance toDelete = null;

		// find corresponding game instance to the player and remove him
		for (GameInstance game : games) {
			if (game.containsPlayer(sessionID)) {
				game.removePlayer(sessionID);
				toDelete = game;
			}
		}

		// If there are no players left in the game instance -> delete it
		if (toDelete != null && toDelete.getPlayerCount() == 0) {
			games.remove(toDelete);
		}
	}

	/**
	 * 
	 * @param sessionID of the user
	 * @return The GameInstance the player is playing in. Returns null when not in a
	 *         game
	 */
	public GameInstance getGameInstanceOfPlayer(String sessionID) {
		GameInstance g = null;
		for (GameInstance game : games) {
			if (game.containsPlayer(sessionID)) {
				g = game;
			}
		}
		return g;
	}

	/**
	 * 
	 * @param game newly created game to be added to the list of games
	 */
	public void addGameInstance(GameInstance game) {
		games.add(game);
	}

	/**
	 * Rolls the dice for the player of sessionID
	 * 
	 * @param sessionID clients sessionID
	 */
	public void rollDice(String sessionID) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		g.rollDice(sessionID);
	}

	/**
	 * Adds dice to bank of player's GameInstance
	 * 
	 * @param sessionID client's sessionID
	 * @param number    which is shown on top of the dice
	 */
	public void addDiceToBank(String sessionID, int number) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		g.addDiceToBank(sessionID, number);
	}

	/**
	 * Remove dice from bank of player's GameInstance
	 * 
	 * @param sessionID client's sessionID
	 * @param number    which is shown on top of the dice
	 */
	public void removeDiceFromBank(String sessionID, int number) {
		GameInstance g = getGameInstanceOfPlayer(sessionID);
		g.removeDiceFromBank(sessionID, number);
	}

	/**
	 * Sets score of player's GameInstance
	 * 
	 * @param sessionID client's sessionID
	 * @param scoreID   the id of the field the score is set to (e.g 0 for aces)
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
		if (g != null) {
			g.updateReady(sessionID, true);
		}
	}
}
