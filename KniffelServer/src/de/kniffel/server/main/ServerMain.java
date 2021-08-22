package de.kniffel.server.main;

import java.io.IOException;

import de.kniffel.server.threads.AdminConsoleThread;
import de.kniffel.server.threads.ServerListenerThread;
import de.kniffel.server.threads.ServerTimeoutListenerThread;
import de.kniffel.server.utils.ConfigManager;
import de.kniffel.server.utils.SQLManager;
import de.kniffel.server.utils.SessionManager;
import de.kniffel.server.utils.SessionManager.Session;

/**
 *
 * This is the main class of this project. This class contains the starting
 * point of the program and brings most of the classes together to let them
 * operate the way they should.
 * 
 * @author KBeck
 * 
 *
 */
public class ServerMain {

	private static int port;
	private static SQLManager sql;
	private static ServerListenerThread listenerThread;
	private static ServerTimeoutListenerThread timeoutThread;

	/**
	 * starting point of the program: 1. loads data from the config 2. creates the
	 * database if it doesn't exist already 3. starts
	 * multiple threads which are the "core" of the server
	 * 
	 * @param args are not used
	 */
	public static void main(String[] args) {

		loadConfig();
		sql.createDatatablesIfNotExisting();

		try {
			listenerThread = new ServerListenerThread(port);
			listenerThread.start();
			timeoutThread = new ServerTimeoutListenerThread();
			timeoutThread.start();
			AdminConsoleThread adminThread = new AdminConsoleThread();
			adminThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * loads values from the config file and create instance of SQLManager learned
	 * from: https://mkyong.com/java/java-properties-file-examples/
	 */
	private static void loadConfig() {
		port = Integer.valueOf(ConfigManager.readFromProperties("server.port"));
		sql = new SQLManager(ConfigManager.readFromProperties("sql.url"), ConfigManager.readFromProperties("sql.user"),
				ConfigManager.readFromProperties("sql.password"));
	}

	/**
	 * @return publicly available instance of SQLManager
	 */
	public static SQLManager getSQLManager() {
		return sql;
	}

	/**
	 * @return port the server is running on
	 */
	public static int getPort() {
		return port;
	}

	/**
	 * @return true if the server is running, otherwise false
	 */
	public static boolean isRunning() {
		return !listenerThread.getServerSocket().isClosed() && listenerThread.getServerSocket().isBound();
	}

	/**
	 * shuts down the server
	 */
	public static void shutdown() {

		for (Session s : SessionManager.getInstance().getSessions().values()) {
			s.getWorkerThread().shutdown();
		}
		listenerThread.shutdown();
		System.out.println("SERVER CLOSED");
		System.exit(0);
	}

}
