package de.kniffel.server.threads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * @author KBeck This class serves as a thread which only listens for incoming
 *         clients and creates a new worker thread for every one of them This
 *         type of architecture can work on requests way faster, because it does
 *         not have to wait for a request to be finished to start a new one
 */
public class ServerListenerThread extends Thread {

	private ServerSocket serverSocket;

	public ServerListenerThread(int port) throws IOException {
		serverSocket = new ServerSocket(port);
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	/**
	 * waits for a request of a client and sends the message over to a new
	 * "woker"-thread, which handles it in detail.
	 */
	@Override
	public void run() {
		while (!serverSocket.isClosed() && serverSocket.isBound()) {
			try {
				System.out.println("Waiting for clients at " + serverSocket.getLocalPort());
				Socket client = serverSocket.accept();

				ServerRequestWorkerThread worker = new ServerRequestWorkerThread(client);
				worker.start();

			} catch (Exception ex) {
				try {
					serverSocket.close();
				} catch (IOException e) {

				}
			}
		}
	}

	/**
	 * gets called whenever "shutdown" is used by an admin
	 */
	public void shutdown() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
