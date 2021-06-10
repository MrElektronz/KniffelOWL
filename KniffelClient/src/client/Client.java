package client;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import application.Main;

public class Client {

	private static final String IP = Main.readProperty("server.ip");
	private static final int PORT = Integer.valueOf(Main.readProperty("server.port"));
	private static String sessionID = "0";
	private static String username = "";
	private static int profilePicID = 0;
	/**
	 * This method also updates the profilePicID
	 * @param username 
	 * @param password not-encrypted password
	 * @return if login was successful return is the session-ID, if not return is null, if wrong username/password its '0'
	 */
	public static String login(String username, String password) {
		try {
		Socket client = new Socket(IP,PORT);
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		//out.writeUTF("$Login;"+username+";"+password);
		out.writeUTF(buildServerCommand("Login",username,encrypt(password)));
		DataInputStream in = new DataInputStream(client.getInputStream());
		String value = in.readUTF();
		in.close();
		out.close();
		client.close();
		if(value.equals("")) {
			return null;
		}
		if(!value.equals("-1")) {
		//successful login
		sessionID = value;
		//update profilePic
		}
		return value;
		}catch(IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static void setUsername(String username) {
		Client.username = username;
	}
	/**
	 * is client-side only
	 * @param profilePicID
	 */
	public static void setProfilePicID(int profilePicID) {
		Client.profilePicID = profilePicID;
	}
	public static String getUsername() {
		return username;
	}
	
	public static int getProfilePicID() {
		return profilePicID;
	}
	
	/**
	 *  is server-side only
	 * @param id
	 */
	public static void sendNewProfilePicID(int id) {
		try {
		Socket client = new Socket(IP,PORT);
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		out.writeUTF(buildServerCommand("SetProf", sessionID,id+""));
		out.close();
		client.close();
		}catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void updateProfilePic() {
		try {
		Socket client = new Socket(IP,PORT);
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		//out.writeUTF("$Login;"+username+";"+password);
		out.writeUTF(buildServerCommand("GetProf",username));
		DataInputStream in = new DataInputStream(client.getInputStream());
		int value = in.readInt();
		in.close();
		out.close();
		client.close();
		setProfilePicID(value);
		}catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * sends a message to the server to delete the current session
	 * TODO: Send client back to 'Scene_Login'
	 */
	public static void logout() {
		if(!sessionID.equals("0")) {
		try {
			System.out.println("mach logout jetzt");
		Socket client = new Socket(IP,PORT);
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		out.writeUTF(buildServerCommand("Logout", sessionID));
		sessionID = "0";
		out.close();
		client.close();
		//Could receive answer from server after logout
		}catch(IOException ex) {
			ex.printStackTrace();
		}
		}
	}
	
	public static String requestUsername() {
		String user = "?";
		if(!sessionID.equals("0")) {
			try {
			Socket client = new Socket(IP,PORT);
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeUTF(buildServerCommand("ReqestUsername", sessionID));
			DataInputStream in = new DataInputStream(client.getInputStream());
			user = in.readUTF();
			out.close();
			in.close();
			client.close();
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
	public static byte requestResetPassword(String username) {
		try {
		Socket client = new Socket(IP,PORT);
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		out.writeUTF(buildServerCommand("RequestResetPW", username));
		DataInputStream in = new DataInputStream(client.getInputStream());
		byte value = in.readByte();
		in.close();
		out.close();
		client.close();
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
	public static byte resetPassword(String username, String password, String pin) {
		password = encrypt(password);
		try {
			Socket client = new Socket(IP,PORT);
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeUTF(buildServerCommand("ResetPW", username,password,pin));
			DataInputStream in = new DataInputStream(client.getInputStream());
			byte value = in.readByte();
			in.close();
			out.close();
			client.close();
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
	public static void pingServer() {
		//System.out.println("ping");
		if(!sessionID.equals("0")) {
			try {
			Socket client = new Socket(IP,PORT);
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			//Ping command: $P;ID
			out.writeUTF(buildServerCommand("P", sessionID));
			out.close();
			client.close();
			}catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @return "0" if no active session
	 */
	public static String getSessionID() {
		return sessionID;
	}
	/**
	 * 
	 * @param username
	 * @param password
	 * @param email
	 * @return 1 if successful, 0 if already exists, 2 format, 3 other error, 4 email already in use, 5 email format
	 */
	public static int register(String username, String password, String email) {
		if(email.contains("@") && email.contains(".")) {
		try {
		Socket client = new Socket(IP,PORT);
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		out.writeUTF(buildServerCommand("Reg", username,encrypt(password),email));
		DataInputStream in = new DataInputStream(client.getInputStream());
		int value = in.readInt();
		in.close();
		out.close();
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
	private static String buildServerCommand(String prefix, String...args ) {
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
	public static String encrypt(String in) {
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
	public static int[] requestLobbyUpdate() {
		try {
			Socket client = new Socket(IP,PORT);
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeUTF(buildServerCommand("LobbyUpdate", sessionID));
			int[] lobbyData = readInts(client.getInputStream());
			out.close();
			client.close();
			return lobbyData;
			}catch(IOException ex) {
				ex.printStackTrace();
				return new int[1];
			}
	}
	
	/**
	 * tells the server that the player is ready to start the game (if he is in a lobby)
	 */
	public static void sendLobbyReadyStatus() {
		try {
			Socket client = new Socket(IP,PORT);
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeUTF(buildServerCommand("LobbyReady", sessionID));
			out.close();
			client.close();
			}catch(IOException ex) {
				ex.printStackTrace();
			}
	}
	
	
	/**
	 * Helper method to read ints from an InputStream
	 * (taken from https://stackoverflow.com/questions/35527516/java-sending-receiving-int-array-via-socket-to-from-a-program-coded-in-c)
	 */
	private static int[] readInts(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);
        try {
        int[] ints = new int[dataIn.readInt()];
        for (int i = 0; i < ints.length; ++i) ints[i] = dataIn.readInt();
        dataIn.close();
        return ints;
        }catch(EOFException ex) {
            dataIn.close();
        	return new int[1];
        }
        
        
    }
	
	/**
	 * tells the server that the player is ready to start the game (if he is in a lobby)
	 */
	public static void sendLeaveLobby() {
		try {
			Socket client = new Socket(IP,PORT);
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeUTF(buildServerCommand("LobbyLeave", sessionID));
			out.close();
			client.close();
			}catch(IOException ex) {
				ex.printStackTrace();
			}
	}
	
	
}
