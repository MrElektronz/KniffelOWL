package de.kniffel.server.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import de.kniffel.server.Server;
import de.kniffel.server.SessionManager;

/**
 * 
 * @author KBeck
 * This class serves as a thread which only listens for incoming clients and creates a new worker thread for every one of them
 * This type of architecture can work on requests way faster, because it does not have to wait for a request to be finished to start a new one
 * TODO: Add ServerCheckTimeoutListener <- checks every ~10 secs if every conencted client can be reached or if the last time seen is >30 seconds
 */
public class ServerTimeoutListenerThread extends Thread{
	
	
	

	
	
	@Override
	public void run() {
		while(Server.isRunning()) {
		
			SessionManager.checkForTimeouts(30);
			
			
			try {
				sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	}

}
