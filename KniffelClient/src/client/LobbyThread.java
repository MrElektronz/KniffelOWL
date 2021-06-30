package client;

import application.LobbyController;

/**
 * This class asks every 2 seconds for a lobby update
 * @author KBeck
 *
 */
public class LobbyThread extends Thread{

	private LobbyController lobby;
	private boolean running;
	public LobbyThread(LobbyController lobby) {
		this.lobby = lobby;
		running = true;
		System.out.println("GO");
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	@Override
	public void run() {
		while(running) {
			//update the lobby client-side
			System.out.println("run lobby");
		lobby.updateLobby(Client.requestLobbyUpdate());
		try {
			//request a new lobby update every 2 seconds
			sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}
}
