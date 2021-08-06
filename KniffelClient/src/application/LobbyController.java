package application;

import java.io.IOException;
import java.util.ArrayList;

import client.Client;
import client.LobbyThread;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import profile.ProfileManager;

public class LobbyController {
	

	@FXML
	Button bReturn;
	@FXML
	Button bReady;
	@FXML
	HBox horbox;
	private boolean isReady;
	
	private Client client;
	public void initialize() {
		isReady = false;
		client = Client.getInstance();
		if(Main.lThread == null) {
		Main.lThread = new LobbyThread(this);
		Main.lThread.start();
		System.out.println("New thread");
		}else {
			Main.lThread.setLobby(this);
		}
		client.addToLobby();
	}
	
	/**
	 * If all players (at least 2) press 'ready'. the game will start. Othwise it will start at 4 players.
	 * @param event
	 * @throws IOException
	 */
	public void onReady(ActionEvent event) throws IOException {
		if(!isReady) {
		isReady = true;
		bReady.setText("Ready");
		bReady.setStyle("-fx-background-color: #CCCC");
		bReturn.setVisible(false);
		client.sendLobbyReadyStatus();
		}
	}
	
	public static void shutdown() {
		
		/*if(Main.lThread != null) {
			Main.lThread.setRunning(false);
			Main.lThread = null;
		}*/
		Client.getInstance().sendLeaveLobby();
		System.out.println("leave lobby1");
	}
	/**
	 * @param event
	 * @throws IOException
	 */
	public void onReturn(ActionEvent event) throws IOException {
		shutdown();
		Main.changeScene("Scene_MainMenu.fxml",1280,720);
	}
	
	public void updateLobby(byte[] lobbyData) {
		//start the game if lobby is ready
		System.out.println(client.getSessionID()+" Lobby Data: "+lobbyData[0]+" "+lobbyData[1]);
		if(lobbyData.length>2) {
			//update the lobby scene
			ArrayList<ImageView> profiles = new ArrayList<ImageView>();
			System.out.println("adding profiles");
			for(int i = 0; i< lobbyData.length-1;i++) {
				ImageView iv = new ImageView(ProfileManager.getInstance().getImage(lobbyData[i])); 
				profiles.add(iv);
			}
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					horbox.getChildren().clear();
					horbox.getChildren().addAll(profiles);
					System.out.println("show em");
				}
				
			});
		}
	}
	
	
}
