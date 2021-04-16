package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Properties;

import application.Main;

public class Client {

	private static final String IP = Main.readProperty("server.ip");
	private static final int PORT = Integer.valueOf(Main.readProperty("server.port"));
	private static String sessionID = "0";
	
	/**
	 * 
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
		sessionID = value;
		}
		return value;
		}catch(IOException ex) {
			return null;
		}
	}
	/**
	 * sends a message to the server to delete the current session
	 * TODO: Send client back to 'Scene_Login'
	 */
	public static void logout() {
		if(!sessionID.equals("0")) {
		try {
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
	
}
