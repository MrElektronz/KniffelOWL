package de.kniffel.server.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * @author KBeck
 * This class serves as a thread which only listens for incoming clients and creates a new worker thread for every one of them
 * This type of architecture can work on requests way faster, because it does not have to wait for a request to be finished to start a new one
 * TODO: Add ServerCheckTimeoutListener <- checks every ~30 secs if every conencted client can be reached
 */
public class ServerListenerThread extends Thread{
	
	
	private ServerSocket serverSocket;
	
	public ServerListenerThread(int port) throws IOException{
		serverSocket = new ServerSocket(port);
	}
	
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	
	//
	@Override
	public void run() {
		while(!serverSocket.isClosed() && serverSocket.isBound()) {
		try {
			System.out.println("Waiting for client at "+serverSocket.getLocalPort());
			Socket client = serverSocket.accept();
			//Server.log("Connection accepted: "+client.getInetAddress());
			
			ServerRequestWorkerThread worker = new ServerRequestWorkerThread(client);
			worker.start();
		
		}catch(Exception ex) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				
			}
		}
	}
	}

}
