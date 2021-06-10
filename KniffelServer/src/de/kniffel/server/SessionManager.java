package de.kniffel.server;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;



/**
 * 
 * @author KBeck
 *
 */
public class SessionManager {

	
	//Has format SessionID,Session(Username, lastTimeSeen)
		private static HashMap<String, Session> sessions;
		
		public static void setup() {
			sessions = new HashMap<String, Session>();
		}
		
		/**
		 * 
		 * @param id
		 * @param username
		 * @return sessionID if username does not already have session, if so: return "-1"
		 */
		public static String addNewSession(String username) {
			
			String id = createNewSessionID();
			Session s = new Session(username);
			if(!sessions.containsValue(s)) {
				sessions.put(id,s);
				return id;
			}else {
				return "-1";
			}
		}
		
		public static Session getSession(String sessionID) {
			return sessions.get(sessionID);
		}
		
		public static void delSession(String id) {
			Session s = sessions.get(id);
			sessions.remove(id);
			
			//if in gameinstance
			if(s.getCurrentGame() != null) {
				GameInstance game = s.getCurrentGame();
				
			}
		}
		
		public static boolean doesSessionExist(String id) {
			return sessions.containsKey(id);
		}
		
		public static void clearAllSessions() {
			sessions.clear();
		}
		
		/**
		 * accepts the ping of a client which is determined to see if a client is still connected to the server
		 */
		public static void acceptPing(String sessionID) {
			if(sessions.containsKey(sessionID)) {
				sessions.get(sessionID).updateLastTimeSeen();
				System.out.println("PING: "+sessions.get(sessionID));
			}
			
		}
		
		public static void printSessions() {
			Object[] keys = sessions.keySet().toArray();
			System.out.println("--------------------------------------");
			for(int i = 0; i<sessions.size();i++) {
			System.out.println(i+": "+keys[i]+" "+sessions.get(keys[i]));
			}
			System.out.println("--------------------------------------");
		}
		
		/**
		 * 
		 * @return new sessionID does not check for duplicates now
		 */
		private static String createNewSessionID() {
			
			    String generatedString = new RandomString(16).nextString();
			    if(doesSessionExist(generatedString)) {
			    	return createNewSessionID();
			    }else {
			    	return generatedString;
			    }
		}
		
		public static int logout(String sessionID) {
			System.out.println("Session "+ sessionID+"("+sessions.get(sessionID)+")"+" deleted");
			delSession(sessionID);
			return 1;
		}
		
		/**
		 * checks every x seconds if every session is still active
		 * @param difference the max difference in seconds between lastTimeSeen(last ping) of Client and now, if exceeded the session will be removed
		 */
		public static void checkForTimeouts(int difference) {
			Object[] keys = sessions.keySet().toArray();
			Instant end = Instant.now();
			for(int i = 0; i< sessions.size();i++) {
				Session s = sessions.get(keys[i]);
				Duration dur = Duration.between(s.getLastTimeSeen(), end);
				if(dur.compareTo(Duration.ofSeconds(difference)) >0) {
					//remove session
					delSession((String)keys[i]);
					Server.log(s.toString() +" timed out");
					printSessions();
				}
				
			}
		}
		
		public static class Session{
			private String username;
			private Instant lastTimeSeen;
			private GameInstance currentGame;
			public Session(String username) {
				this.username = username;
				lastTimeSeen = Instant.now();
				currentGame = null;
			}
			
			public String getUsername() {
				return username;
			}
			
			public Instant getLastTimeSeen() {
				return lastTimeSeen;
			}
			
			public void updateLastTimeSeen() {
				lastTimeSeen = Instant.now();
			}
			
			public void setCurrentGame(GameInstance currentGame) {
				this.currentGame = currentGame;
			}
			public GameInstance getCurrentGame() {
				return currentGame;
			}
			
			/**
			 * Override equals in Order to use containsValue of class Hashmap
			 */
			@Override
			public boolean equals(Object arg0) {
				Session s = (Session)arg0;
				return username.equals(s.getUsername());
			}
			
	
			@Override
			public String toString() {
				return username+" last seen at: "+lastTimeSeen.toString();
			}
			
		}
	
}
