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
import java.util.HashMap;
import java.util.Properties;

import de.kniffel.server.threads.ServerListenerThread;
import de.kniffel.server.threads.ServerTimeoutListenerThread;

public class Server {
	
	private static int port;
	private static SQLManager sql;
	private static ServerListenerThread listenerThread;
	
	
	public static void main(String[] args) {
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
