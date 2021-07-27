package client;

import java.io.IOException;

import application.LobbyController;
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
		client.addToLobby();
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	@Override
	public void run() {
		while(running&&!client.getSocket().isClosed()) {
			//update the lobby client-side
			System.out.println("run lobby");
		//lobby.updateLobby(Client.getInstance().requestLobbyUpdate());
			String received = "";
			try {
				received = client.getIn().readUTF();
			}catch(Exception ex) {
				
			}
			String[] args = received.split(";");
			System.out.println(received);
			String command = args[0];
			
			if(!received.equals("")) {
			System.out.println(received);
			}
			if(command.equals("!LobbyUpdate")) {
				System.out.println("YWWWWWWWWWWWWWWW");
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
				
				//!Roll;byte[] of 5 random dice numbers, this is used so rolling the dice is done through the server :)
			}else if(command.equals("!Roll")) {
				
			}
		try {
			sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		/*while(running&&!client.getSocket().isClosed()) {
			
			String received = "";
				try {
					received = client.getIn().readUTF();
				}catch(Exception ex) {
					
				}
				String[] args = received.split(";");
				System.out.println(received);
				String command = args[0];
				
				if(!received.equals("")) {
				System.out.println(received);
				}
				
				if(command.equals("!test")) {
					System.out.println("YEEEEEEEEEEEEESS GOOOO");
				}else if(command.equals("!LobbyUpdate")) {
					try {
						//update the lobby every 10ms
						lobby.updateLobby((int[])Serializer.fromString(args[1]));
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
			try {
				sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}
}
