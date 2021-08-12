package de.kniffel.client.threads;

import de.kniffel.client.utils.Client;

/**
 * This class sends a small message to the server every 9 seconds to let the server know he is still connected, 
 * if the last time the server received one such message is longer than 30 seconds, the server deletes the session
 * and the user has to log in again (only happens if client has a valid sessionID)
 * @author KBeck
 *
 */
public class TimeoutThread extends Thread{

	private boolean running;
	
	public TimeoutThread() {
		running = true;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	@Override
	public void run() {

		while(running) {
		Client.getInstance().pingServer();
		try {
			sleep(9000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}
}
