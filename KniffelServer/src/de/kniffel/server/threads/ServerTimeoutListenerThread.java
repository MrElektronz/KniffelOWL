package de.kniffel.server.threads;

import de.kniffel.server.main.ServerMain;
import de.kniffel.server.utils.SessionManager;

/**
 * 
 * Checks every ~10 secs if every connected client can be reached or if the last
 * time seen is >120 seconds.
 * 
 * @author KBeck
 * 
 */
public class ServerTimeoutListenerThread extends Thread {

	@Override
	public void run() {
		while (ServerMain.isRunning()) {

			SessionManager.getInstance().checkForTimeouts(120);

			try {
				sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
