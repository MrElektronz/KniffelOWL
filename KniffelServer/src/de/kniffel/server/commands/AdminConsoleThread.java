package de.kniffel.server.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.kniffel.server.Server;

public class AdminConsoleThread extends Thread{

	private boolean running;
	public AdminConsoleThread() {
		running = true;
	}
	
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
				Server.shutdown();
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
