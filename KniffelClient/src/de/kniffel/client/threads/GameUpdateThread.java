package de.kniffel.client.threads;

import java.io.IOException;

import de.kniffel.client.controllers.LobbyController;
import de.kniffel.client.main.ClientMain;
import de.kniffel.client.serialize.OnlineSessionWrapper;
import de.kniffel.client.serialize.Serializer;
import de.kniffel.client.utils.Client;
import javafx.application.Platform;

/**
 * This class asks every 2 seconds for a lobby update 
 * TEST: TRY SERVER SENDING TO CLIENT WITHOUT REQUEST
 * @author KBeck
 *
 */
public class GameUpdateThread extends Thread{

	private LobbyController lobby;
	private boolean running;
	private Client client;
	public GameUpdateThread(LobbyController lobby) {
		client = Client.getInstance();
		this.lobby = lobby;
		running = true;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public void setLobby(LobbyController lobby) {
		this.lobby = lobby;
	}
	
	@Override
	public void run() {
		while(running&&!client.getSocket().isClosed()) {
			
			//update the lobby client-side
			String received = "";
			try {
				received = client.getIn().readUTF();
			}catch(Exception ex) {
				
			}
			
			String[] args = received.split(";");
			String command = args[0];
			
			if(!received.equals("")) {
			}
			if(command.equals("!LobbyUpdate")) {
				try {
					lobby.updateLobby(Serializer.fromStringToByteArr(args[1]));
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//!GameStart;SerializedObjectWhichContains:All Players,All Stats,current player,active dice numbers, bank dice numbers
			}else if(command.equals("!GameUpdate")) {
					OnlineSessionWrapper os = OnlineSessionWrapper.deserialize(received.replace("!GameUpdate;", ""));
					ClientMain.updateSession(os);
				//!GameEnd;winner
			}else if(command.equals("!GameEnd")) {
				ClientMain.endGame();
				String winner = args[1];
				
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							if(winner.equals(client.getUsername())) {
							try {
								ClientMain.changeToEndScreen("You have won", "#CC8000");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}else {
							try {
								ClientMain.changeToEndScreen("You have lost", "#CC1F00");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}						
					
					}
					});
			
				
			}
		try {
			sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}
}
