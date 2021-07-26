package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;

import application.Main;

public class Client {

	private final String IP = Main.readProperty("server.ip");
	private final int PORT = Integer.valueOf(Main.readProperty("server.port"));
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
	 * @return the one instance of this class
	 */
	public static Client getInstance() {
		if(instance == null) {
			instance = new Client();
		}
		return instance;
	}

	//Creates connection if not already exists
	private void initClient() {
		if(client == null) {
			try {
				client = new Socket(IP,PORT);
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
	 * @param username 
	 * @param password not-encrypted password
	 * @return if login was successful return is the session-ID, if not return is null, if wrong username/password its '0'
	 */
	public String login(String username, String password) {
		if(username.equals("") || username == null || password.equals("") || password == null) {
			return null;
		}
		try {
			initClient();
			//out.writeUTF("$Login;"+username+";"+password);
			out.writeUTF(buildServerCommand("Login",username,encrypt(password)));
			String value = in.readUTF();
			System.out.println("got value: "+value+" in return");
			//client.close();
			if(value.equals("")) {
				return null;
			}
			if(!value.equals("-1")) {
				//successful login
				sessionID = value;
				//update profilePic
			}
			out.flush();
			return value;
		}catch(IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * is client-side only
	 * @param profilePicID resembles id of profile picture
	 */
	public void setProfilePicID(int profilePicID) {
		this.profilePicID = profilePicID;
	}
	public String getUsername() {
		return username;
	}

	public int getProfilePicID() {
		return profilePicID;
	}

	/**
	 *  is server-side only
	 * @param id resembles id of profile picture
	 */
	public void sendNewProfilePicID(int id) {
		try {
			initClient();
			out.writeUTF(buildServerCommand("SetProf", sessionID,id+""));
			out.flush();
		}catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	public void updateProfilePic() {
		try {
			initClient();
			//out.writeUTF("$Login;"+username+";"+password);
			out.writeUTF(buildServerCommand("GetProf",username));
			System.out.println("SENDING GET PROF");
			int value = in.readInt();
			//client.close();
			setProfilePicID(value);
			out.flush();
		}catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * sends a message to the server to delete the current session
	 * TODO: Send client back to 'Scene_Login'
	 */
	public void logout() {
		if(!sessionID.equals("0")) {
			try {
				System.out.println("mach logout jetzt");
				initClient();
				out.writeUTF(buildServerCommand("Logout", sessionID));
				out.flush();
				sessionID = "0";
				in.close();
				out.close();
				client.close();
				//Could receive answer from server after logout
			}catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public String requestUsername() {
		String user = "?";
		if(!sessionID.equals("0")) {
			try {
				initClient();
				out.writeUTF(buildServerCommand("ReqestUsername", sessionID));
				out.flush();
				user = in.readUTF();
				//client.close();
				//Could receive answer from server after logout
			}catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		return user;
	}
	/**
	 * 
	 * @param username
	 * @return 1 if reset code was send, 0 if username not found, 2 other error
	 */
	public byte requestResetPassword(String username) {
		if(username.equals("") || username == null) {
			return 0;
		}
		try {
			initClient();
			out.writeUTF(buildServerCommand("RequestResetPW", username));
			out.flush();
			byte value = in.readByte();
			//client.close();
			return value;
		}catch(IOException ex) {
			ex.printStackTrace();
			return 2;
		}
	}


	/**
	 * 
	 * @param username
	 * @param password decrypted
	 * @param pin
	 * @return 1 if password was reset, 0 if username not found, 2 if other error, 3 if pin wrong
	 */
	public byte resetPassword(String username, String password, String pin) {
		password = encrypt(password);
		try {
			initClient();
			out.writeUTF(buildServerCommand("ResetPW", username,password,pin));
			out.flush();
			byte value = in.readByte();
			//client.close();
			return value;
		}catch(IOException ex) {
			ex.printStackTrace();
			return 2;
		}
	}

	/**
	 * sends a simple message to the server to let it know the client is still connected.
	 * The Server refreshes the variable 'lastTimeSeen' for the current session if the message was received by it
	 * if not: The server deletes the current session after multiple pings did not come through
	 */
	public void pingServer() {
		//System.out.println("ping");
		if(!sessionID.equals("0")) {
			try {
				initClient();
				//Ping command: $P;ID
				out.writeUTF(buildServerCommand("P", sessionID));
				out.flush();
				//client.close();
			}catch(IOException ex) {
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
	 * @param username
	 * @param password
	 * @param email
	 * @return 1 if successful, 0 if already exists, 2 format, 3 other error, 4 email already in use, 5 email format
	 */
	public int register(String username, String password, String email) {
		if(email.contains("@") && email.contains(".")) {
			try {
				initClient();
				out.writeUTF(buildServerCommand("Reg", username,encrypt(password),email));
				out.flush();
				int value = in.readInt();
				return value;
			}catch(IOException ex) {
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
	private String buildServerCommand(String prefix, String...args ) {
		String s = "$"+prefix+";";
		for(String a : args) {
			s+=a+";";
		}
		return s.substring(0, s.length()-1);
	}


	/**
	 * 
	 * @param in 
	 * @return the String 'in' but encrypted with the MD5-Algorithm or "" if the password contains a space
	 */
	public String encrypt(String in) {
		if(in.contains(" ") || in.equals("") || in.length()<4) {
			return "";
		}
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(in.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			String hashtext = bigInt.toString(16);

			while(hashtext.length()<32) {
				hashtext= "0"+hashtext;
			}
			return hashtext;
		}catch(Exception e) {
			return "";
		}
	}

	/**
	 * @return has a size of 5, with index 0-3 being the player profile id's and if index 4 = 1 the game begins
	 * (the lobby is full or everyone is ready)
	 */
	public int[] requestLobbyUpdate() {
		try {
			initClient();
			out.writeUTF(buildServerCommand("LobbyUpdate", sessionID));
			out.flush();
			int[] lobbyData = readInts();
			//client.close();
			return lobbyData;
		}catch(IOException ex) {
			ex.printStackTrace();
			return new int[1];
		}
	}

	/**
	 * tells the server that the player is ready to start the game (if he is in a lobby)
	 */
	public void sendLobbyReadyStatus() {
		try {
			initClient();
			out.writeUTF(buildServerCommand("LobbyReady", sessionID));
			//client.close();
		}catch(IOException ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * Helper method to read ints from an InputStream
	 * (taken from https://stackoverflow.com/questions/35527516/java-sending-receiving-int-array-via-socket-to-from-a-program-coded-in-c)
	 */
	private int[] readInts() throws IOException {
		try {
			int[] ints = new int[in.readInt()];
			for (int i = 0; i < ints.length; ++i) ints[i] = in.readInt();
			return ints;
		}catch(EOFException ex) {
			return new int[1];
		}


	}

	/**
	 * tells the server that the player is ready to start the game (if he is in a lobby)
	 */
	public void sendLeaveLobby() {
		try {
			initClient();
			out.writeUTF(buildServerCommand("LobbyLeave", sessionID));
			out.flush();
			//client.close();
		}catch(IOException ex) {
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


}
