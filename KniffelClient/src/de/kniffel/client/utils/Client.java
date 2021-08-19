package de.kniffel.client.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;

import de.kniffel.client.main.ClientMain;

/**
 * This class handles all requests the client sends over to the server (e.g.
 * login or logout)
 * 
 * @author KBeck
 *
 */
public class Client {

	private final String IP = ClientMain.readProperty("server.ip");
	private final int PORT = Integer.valueOf(ClientMain.readProperty("server.port"));
	private String sessionID = "0";
	private String username = "";
	private int profilePicID = 0;
	private Socket client;
	private DataOutputStream out;
	private DataInputStream in;
	private static Client instance;

	/**
	 * private constructor, so no class can instanciate from Client
	 */
	private Client() {

	}

	/**
	 * We are using the Singleton design pattern for this class
	 * 
	 * @return the one instance of this class
	 */
	public static Client getInstance() {
		if (instance == null) {
			instance = new Client();
		}
		return instance;
	}

	// Creates connection if not already exists
	private void initClient() {
		if (client == null) {
			try {
				client = new Socket(IP, PORT);
				out = new DataOutputStream(client.getOutputStream());
				in = new DataInputStream(client.getInputStream());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method also updates the profilePicID
	 * 
	 * @param username of the client
	 * @param password not-encrypted password
	 * @return if login was successful return is the session-ID, if not return is
	 *         null, if wrong username/password its '0'
	 */
	public String login(String username, String password) {
		if (username.equals("") || username == null || password.equals("") || password == null) {
			return null;
		}
		try {
			initClient();
			// out.writeUTF("$Login;"+username+";"+password);
			out.writeUTF(buildServerCommand("Login", username, encrypt(password)));
			String value = in.readUTF();
			// client.close();
			if (value.equals("")) {
				return null;
			}
			if (!value.equals("-1")) {
				// successful login
				sessionID = value;
				// setting username
				this.username = username;
			}
			out.flush();
			return value;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * is client-side only
	 * 
	 * @param profilePicID resembles id of profile picture
	 */
	public void setProfilePicID(int profilePicID) {
		this.profilePicID = profilePicID;
	}

	public String getUsername() {
		if (username.equals("")) {
			username = requestUsername();
		}
		return username;
	}

	public int getProfilePicID() {
		return profilePicID;
	}

	/**
	 * is server-side only
	 * 
	 * @param id resembles id of profile picture
	 */
	public void sendNewProfilePicID(int id) {
		try {
			initClient();
			out.writeUTF(buildServerCommand("SetProf", sessionID, id + ""));
			out.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void updateProfilePic() {
		try {
			initClient();
			// out.writeUTF("$Login;"+username+";"+password);
			out.writeUTF(buildServerCommand("GetProf", getUsername()));
			int value = in.readInt();
			// client.close();
			setProfilePicID(value);
			out.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * sends a message to the server to delete the current session TODO: Send client
	 * back to 'Scene_Login'
	 */
	public void logout() {
		if (!sessionID.equals("0")) {
			try {
				initClient();
				out.writeUTF(buildServerCommand("Logout", sessionID));
				out.flush();
				sessionID = "0";
				in.close();
				out.close();
				client.close();
				// Could receive answer from server after logout
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * This method is not in use right now, but can become handy in the future (The
	 * player can change his username through memory editing and this provides a
	 * safe way to get his real username)
	 * 
	 * @return the client's username, as stored on the server
	 */
	public String requestUsername() {
		String user = "?";
		if (!sessionID.equals("0")) {
			try {
				initClient();
				out.writeUTF(buildServerCommand("RequestUsername", sessionID));
				out.flush();
				user = in.readUTF();
				// client.close();
				// Could receive answer from server after logout
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			System.out.println("no user request");
		}
		return user;
	}

	/**
	 * 
	 * @param username of the account which password should be reset
	 * @return 1 if reset code was send, 0 if username not found, 2 other error
	 */
	public byte requestResetPassword(String username) {
		if (username.equals("") || username == null) {
			return 0;
		}
		try {
			initClient();
			out.writeUTF(buildServerCommand("RequestResetPW", username));
			out.flush();
			byte value = in.readByte();
			// client.close();
			return value;
		} catch (IOException ex) {
			ex.printStackTrace();
			return 2;
		}
	}

	/**
	 * 
	 * @param username of the account
	 * @param password decrypted
	 * @param pin      which was send by e-mail
	 * @return 1 if password was reset, 0 if username not found, 2 if other error, 3
	 *         if pin wrong
	 */
	public byte resetPassword(String username, String password, String pin) {
		password = encrypt(password);
		try {
			initClient();
			out.writeUTF(buildServerCommand("ResetPW", username, password, pin));
			out.flush();
			byte value = in.readByte();
			// client.close();
			return value;
		} catch (IOException ex) {
			ex.printStackTrace();
			return 2;
		}
	}

	/**
	 * sends a simple message to the server to let it know the client is still
	 * connected. The Server refreshes the variable 'lastTimeSeen' for the current
	 * session if the message was received by it if not: The server deletes the
	 * current session after multiple pings did not come through
	 */
	public void pingServer() {
		if (!sessionID.equals("0")) {
			try {
				initClient();
				// Ping command: $P;ID
				out.writeUTF(buildServerCommand("P", sessionID));
				out.flush();
				// client.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @return "0" if no active session
	 */
	public String getSessionID() {
		return sessionID;
	}

	/**
	 * 
	 * @param username of the newly created account
	 * @param password of the newly created account
	 * @param email    of the newly created account
	 * @return 1 if successful, 0 if already exists, 2 format, 3 other error, 4
	 *         email already in use, 5 email format
	 */
	public int register(String username, String password, String email) {
		if (email.contains("@") && email.contains(".")) {
			try {
				initClient();
				out.writeUTF(buildServerCommand("Reg", username, encrypt(password), email));
				out.flush();
				int value = in.readInt();
				return value;
			} catch (IOException ex) {
				return 3;
			}
		}
		return 5;
	}

	/**
	 * 
	 * @param prefix
	 * @param args
	 * @return string of syntax: $prefix;args[0];args[1]...
	 */
	private String buildServerCommand(String prefix, String... args) {
		String s = "$" + prefix + ";";
		for (String a : args) {
			s += a + ";";
		}
		return s.substring(0, s.length() - 1);
	}

	/**
	 * 
	 * @param in string to encrypt
	 * @return the String 'in' but encrypted with the MD5-Algorithm or "" if the
	 *         password contains a space
	 */
	public String encrypt(String in) {
		if (in.contains(" ") || in.equals("") || in.length() < 4) {
			return "";
		}
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(in.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			String hashtext = bigInt.toString(16);

			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (Exception e) {
			return "";
		}
	}

	public void addToLobby() {
		try {
			initClient();
			out.writeUTF(buildServerCommand("LobbyAdd", sessionID));
			/*
			 * This answer doesnt come anymore, now the server will always send data try {
			 * lobbyData = Serializer.fromStringToByteArr(in.readUTF().split(";")[1]); }
			 * catch (ClassNotFoundException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */
			out.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * tells the server that the player is ready to start the game (if he is in a
	 * lobby)
	 */
	public void sendLobbyReadyStatus() {
		try {
			initClient();
			out.writeUTF(buildServerCommand("LobbyReady", sessionID));
			// client.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * If successful, send new game update to all clients
	 */
	public void requestRollDice() {
		try {
			initClient();
			out.writeUTF(buildServerCommand("RequestRoll", sessionID));
			out.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * If successful, send new game update to all clients
	 * 
	 * @param number to add to bank
	 */
	public void requestAddBankDice(int number) {
		try {
			initClient();
			out.writeUTF(buildServerCommand("RequestAddBank", sessionID, "" + number));
			out.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * If successful, send new game update to all clients
	 * 
	 * @param number to remove from bank
	 */
	public void requestRemoveBankDice(int number) {
		try {
			initClient();
			out.writeUTF(buildServerCommand("RequestRemoveBank", sessionID, "" + number));
			out.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * If successful, send new game update to all clients
	 * 
	 * @param scoreID the score field id which is selected
	 */
	public void selectScore(int scoreID) {
		try {
			initClient();
			out.writeUTF(buildServerCommand("SelectScore", sessionID, "" + scoreID));
			out.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * tells the server that the player is ready to start the game (if he is in a
	 * lobby)
	 */
	public void sendLeaveLobby() {
		try {
			initClient();
			out.writeUTF(buildServerCommand("LobbyLeave", sessionID));
			out.flush();
			// client.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 
	 * @return DataInputStream of Socket
	 */
	public DataInputStream getIn() {
		return in;
	}

	/**
	 * 
	 * @return the client's socket
	 */
	public Socket getSocket() {
		return client;
	}

}
