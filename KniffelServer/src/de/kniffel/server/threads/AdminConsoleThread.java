package de.kniffel.server.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.kniffel.server.main.ServerMain;

/**
 * A thread which checks for console input of an admin.
 * 
 * @author KBeck
 *
 */
public class AdminConsoleThread extends Thread{

	private boolean running;
	
	/**
	 * Default constructor, automatically sets "running" to true
	 */
	public AdminConsoleThread() {
		running = true;
	}
	
	/**
	 * 
	 * @param running set to false to stop the thread
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	
	
	
	/**
	 * This is the run method of the AdminConsoleThread, which checks if an admin uses 'exit' to shut the server down
	 */
	@Override
	public void run() {
		
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		
		String read = "";
		
		while(running) {

			try {
				read = buffer.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if(read.equalsIgnoreCase("quit") || read.equalsIgnoreCase("exit") || read.equalsIgnoreCase("shutdown")) {
				running = false;
				ServerMain.shutdown();
			}else {
				System.out.println("Use 'exit' to close the server");
			}
			
			try {
				sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
}
