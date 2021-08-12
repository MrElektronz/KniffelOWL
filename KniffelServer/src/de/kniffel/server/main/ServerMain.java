package de.kniffel.server.main;

import java.io.IOException;

import de.kniffel.server.threads.AdminConsoleThread;
import de.kniffel.server.threads.ServerListenerThread;
import de.kniffel.server.threads.ServerTimeoutListenerThread;
import de.kniffel.server.utils.ConfigManager;
import de.kniffel.server.utils.SQLManager;
import de.kniffel.server.utils.SessionManager;
import de.kniffel.server.utils.SessionManager.Session;

public class ServerMain {
	
	private static int port;
	private static SQLManager sql;
	private static ServerListenerThread listenerThread;
	private static ServerTimeoutListenerThread timeoutThread;
	
	
	public static void main(String[] args) {
		
		loadConfig();
		sql.createDatatablesIfNotExisting();
		SessionManager.getInstance().setup();

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
	
	/**
	 *	shuts down the server
	 */ 
	public static void shutdown() {
		
		for(Session s : SessionManager.getInstance().getSessions().values()) {
			s.getWorkerThread().shutdown();
		}
		listenerThread.shutdown();
		timeoutThread.interrupt();
		System.out.println("SERVER CLOSED");
		System.exit(0);
	}

	
	
}
