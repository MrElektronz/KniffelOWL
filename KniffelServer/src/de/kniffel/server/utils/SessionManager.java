package de.kniffel.server.utils;

import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

import de.kniffel.server.main.GameFinder;
import de.kniffel.server.main.GameInstance;
import de.kniffel.server.threads.ServerRequestWorkerThread;

/**
 * 
 * @author KBeck
 * 
 *         This class has the important task to manage all sessions, which means
 *         managing all logins and logouts/disconnects
 *
 */
public class SessionManager {

	private static SessionManager instance;

	/*
	 * private constructor, so no other class can instanciate from SessionManager
	 */
	private SessionManager() {
	}

	/**
	 * 
	 * @return the one and only instance of SessionManager
	 */
	public static SessionManager getInstance() {
		if (instance == null) {
			instance = new SessionManager();
		}
		return instance;
	}

	/**
	 * 
	 * @return HashMap of a clone of all sessions (which means read-only)
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Session> getSessions() {
		return (HashMap<String, Session>) sessions.clone();
	}

	// Has format SessionID,Session(Username, lastTimeSeen)
	private HashMap<String, Session> sessions;

	public void setup() {
		sessions = new HashMap<String, Session>();
	}

	/**
	 * 
	 * @param id
	 * @param username
	 * @return sessionID if username does not already have session, if so: return
	 *         "-1"
	 */
	public String addNewSession(String username, ServerRequestWorkerThread wt) {

		String id = createNewSessionID();
		Session s = new Session(username, wt);
		if (!sessions.containsValue(s)) {
			sessions.put(id, s);
			return id;
		} else {
			return "-1";
		}
	}

	/**
	 * 
	 * @param sessionID
	 * @return "Session"-object of the given sessionID
	 */
	public Session getSession(String sessionID) {
		return sessions.get(sessionID);
	}

	/**
	 * 
	 * @param wThread
	 * @return "Session"-object of the given ServerRequestWorkerThread
	 */
	public Session getSession(ServerRequestWorkerThread wThread) {
		for (Session s : sessions.values()) {
			if (s.getWorkerThread().equals(wThread)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param id
	 * 
	 *           Delete session with the given id
	 */
	public void delSession(String id) {
		Session s = sessions.get(id);
		sessions.remove(id);

		// if in gameinstance
		if (s.getCurrentGame() != null) {
			GameFinder.getInstance().removePlayerFromLobby(id);
		}
	}

	/**
	 * 
	 * @param session
	 * 
	 *                Delete the given session
	 */
	public void delSession(Session session) {
		String id = "";
		for (String sID : sessions.keySet()) {
			if (sessions.get(sID).equals(session)) {
				id = sID;
			}
		}
		sessions.remove(id);

		// if in gameinstance
		if (session.getCurrentGame() != null) {
			GameFinder.getInstance().removePlayerFromLobby(id);
		}
	}

	/**
	 * 
	 * @param id
	 * @return true if session with given id exists, false if otherwise
	 */
	public boolean doesSessionExist(String id) {
		return sessions.containsKey(id);
	}

	/**
	 * accepts the ping of a client which is determined to see if a client is still
	 * connected to the server
	 */
	public void acceptPing(String sessionID) {
		if (sessions.containsKey(sessionID)) {
			sessions.get(sessionID).updateLastTimeSeen();
		}

	}

	/**
	 * useful function to print details about each session
	 */
	public void printSessions() {
		Object[] keys = sessions.keySet().toArray();
		System.out.println("--------------------------------------");
		for (int i = 0; i < sessions.size(); i++) {
			System.out.println(i + ": " + keys[i] + " " + sessions.get(keys[i]));
		}
		System.out.println("--------------------------------------");
	}

	/**
	 * 
	 * @return new sessionID does not check for duplicates now
	 */
	private String createNewSessionID() {

		String generatedString = new RandomString(16).nextString();
		if (doesSessionExist(generatedString)) {
			return createNewSessionID();
		} else {
			return generatedString;
		}
	}

	/**
	 * 
	 * @param sessionID
	 * 
	 *                  logs out the session with the given sessionID
	 */
	public int logout(String sessionID) {
		System.out.println("Session " + sessionID + "(" + sessions.get(sessionID) + ")" + " deleted");
		delSession(sessionID);
		return 1;
	}

	/**
	 * 
	 * @param session
	 * @return 1 if session was deleted, 0 if session does not exist
	 * 
	 *         logs out the given session
	 */
	public int logout(Session session) {
		if (session != null) {
			System.out.println("Session " + session.toString() + " deleted");
			delSession(session);
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * checks every x seconds if every session is still active
	 * 
	 * @param difference the max difference in seconds between lastTimeSeen(last
	 *                   ping) of Client and now, if exceeded the session will be
	 *                   removed
	 */
	public void checkForTimeouts(int difference) {
		Object[] keys = sessions.keySet().toArray();
		Instant end = Instant.now();
		for (int i = 0; i < sessions.size(); i++) {
			Session s = sessions.get(keys[i]);
			Duration dur = Duration.between(s.getLastTimeSeen(), end);
			if (dur.compareTo(Duration.ofSeconds(difference)) > 0) {

				// remove session
				delSession((String) keys[i]);
				printSessions();
			}

		}
	}

	/**
	 * 
	 * @author KBeck
	 * 
	 *         Session class which stores data about a logged in session (such as
	 *         the username, the game instance the user is in and so on)
	 *
	 */
	public class Session {
		private String username;
		private Instant lastTimeSeen;
		private GameInstance currentGame;
		private ServerRequestWorkerThread wt;

		public Session(String username, ServerRequestWorkerThread wt) {
			this.username = username;
			lastTimeSeen = Instant.now();
			currentGame = null;
			this.wt = wt;
		}

		public String getUsername() {
			return username;
		}

		public Socket getClient() {
			return wt.getClient();
		}

		/**
		 * 
		 * @return the ServerRequestWorkerThread which can be used to send data directly
		 *         to the client
		 */
		public ServerRequestWorkerThread getWorkerThread() {
			return wt;
		}

		/**
		 * 
		 * @return "Instant"-object which stores the last time the user has pinged the
		 *         server
		 */
		public Instant getLastTimeSeen() {
			return lastTimeSeen;
		}

		/**
		 * gets called whenever the client pings the server
		 */
		public void updateLastTimeSeen() {
			lastTimeSeen = Instant.now();
		}

		/**
		 * 
		 * @param currentGame the new game instance the user has joined
		 */
		public void setCurrentGame(GameInstance currentGame) {
			this.currentGame = currentGame;
		}

		/**
		 * 
		 * @return the current game instance
		 */
		public GameInstance getCurrentGame() {
			return currentGame;
		}

		/**
		 * Override equals in Order to use containsValue of class Hashmap
		 */
		@Override
		public boolean equals(Object arg0) {
			Session s = (Session) arg0;
			return username.equals(s.getUsername());
		}

		@Override
		public String toString() {
			return username + " last seen at: " + lastTimeSeen.toString();
		}

	}

}
