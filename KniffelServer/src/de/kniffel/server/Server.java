package de.kniffel.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import de.kniffel.serialize.OnlinePlayerWrapper;
import de.kniffel.serialize.OnlineSessionWrapper;
import de.kniffel.server.threads.ServerListenerThread;
import de.kniffel.server.threads.ServerTimeoutListenerThread;

public class Server {
	
	private static int port;
	private static SQLManager sql;
	private static ServerListenerThread listenerThread;
	
	
	public static void main(String[] args) {
	/*	
		OnlinePlayerWrapper wr = new OnlinePlayerWrapper("Peterü", (byte)2);
		wr.setScore(0, 3);
		wr.setScore(2, 9);
		wr.setScore(12, 23);
		OnlinePlayerWrapper wr2 = new OnlinePlayerWrapper("Ellen", (byte)6);
		wr2.setScore(4,76);
		OnlinePlayerWrapper wr3 = new OnlinePlayerWrapper("Pseses", (byte)1);
		wr3.setScore(6, 23);
		OnlinePlayerWrapper wr4 = new OnlinePlayerWrapper("Sasa", (byte)3);
		wr4.setScore(12, 99);
		OnlineSessionWrapper session = new OnlineSessionWrapper(wr, wr2, wr3, wr4);
		System.out.println(session.serialize());
		
		OnlineSessionWrapper s2 = OnlineSessionWrapper.deserialize(session.serialize());
		
		System.out.println(s2.serialize());*/
		
		loadConfig();
		sql.createDatatablesIfNotExisting();
		SessionManager.getInstance().setup();

		try {
			listenerThread = new ServerListenerThread(port);
			listenerThread.start();
			ServerTimeoutListenerThread timeoutThread = new ServerTimeoutListenerThread();
			timeoutThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	
	
	public static void log(String param) {
		System.out.println("SERVER: "+param);
	}
	
	/**
	 * loads values from the config file and create instance of SQLManager
	 * learned from: https://mkyong.com/java/java-properties-file-examples/
	 */
	private static void loadConfig() {
		
			port = Integer.valueOf(ConfigManager.readFromProperties("server.port"));
			sql = new SQLManager(ConfigManager.readFromProperties("sql.url"), ConfigManager.readFromProperties("sql.user"), ConfigManager.readFromProperties("sql.password"));
		
	}
	
	/**
	 * 
	 * @return publicly availabe instance of SQLManager
	 */
	public static SQLManager getSQLManager() {
		return sql;
	}
	
	public static int getPort() {
		return port;
	}
	
	public static boolean isRunning() {
		return !listenerThread.getServerSocket().isClosed() && listenerThread.getServerSocket().isBound();
	}

	
	
}
