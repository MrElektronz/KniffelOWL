package client;

import java.io.IOException;

import application.LobbyController;
import application.Main;
import application.OnlineGameBoardController;
import de.client.serialize.OnlineSessionWrapper;
import de.client.serialize.Serializer;

/**
 * This class asks every 2 seconds for a lobby update 
 * TEST: TRY SERVER SENDING TO CLIENT WITHOUT REQUEST
 * @author KBeck
 *
 */
public class LobbyThread extends Thread{

	private LobbyController lobby;
	private boolean running;
	private Client client;
	public LobbyThread(LobbyController lobby) {
		client = Client.getInstance();
		this.lobby = lobby;
		running = true;
		System.out.println("GO");
		//lobby.updateLobby(client.addToLobby());
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
		    //lobby.updateLobby(Client.getInstance().requestLobbyUpdate());
			String received = "";
			try {
				received = client.getIn().readUTF();
			}catch(Exception ex) {
				
			}
			
			String[] args = received.split(";");
			String command = args[0];
			
			if(!received.equals("")) {
			System.out.println("received: "+received);
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
					Main.updateSession(os);
					System.out.println("SERVER: "+os.serialize());
				//!GameEnd;winner
			}else if(command.equals("!GameEnd")) {
				String winner = args[1];
				if(winner.equals(client.getUsername())) {
					try {
						Main.changeToEndScreen("You have won", "#CC8000");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else {
					try {
						Main.changeToEndScreen("You have lost", "#CC1F00");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
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
