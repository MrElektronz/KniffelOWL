package de.kniffel.client.controllers;

import java.io.IOException;
import java.util.ArrayList;

import de.kniffel.client.main.ClientMain;
import de.kniffel.client.threads.GameUpdateThread;
import de.kniffel.client.utils.Client;
import de.kniffel.client.utils.ProfileManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Controller class of the Lobby-Scene. Updates the player display and start the
 * GameUpdateThread.
 * 
 * @author KBeck
 * 
 *
 */

public class LobbyController {

	@FXML
	Button bReturn;
	@FXML
	Button bReady;
	@FXML
	HBox horbox;

	private boolean isReady;
	private Client client;

	/**
	 * gets called whenever a scene with this controller assigned to it, starts
	 */
	public void initialize() {
		isReady = false;
		client = Client.getInstance();

		// Start a new game update thread if none is running
		if (ClientMain.lThread == null) {
			ClientMain.lThread = new GameUpdateThread(this);
			ClientMain.lThread.start();
		} else {
			// If a thread is running, set the new lobby as reference
			ClientMain.lThread.setLobby(this);
		}

		// Tells the server to add the client to a lobby
		client.addToLobby();
	}

	/**
	 * If all players (at least 2) press 'ready'. the game will start. Othwise it
	 * will start at 4 players.
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onReady(ActionEvent event) throws IOException {
		if (!isReady) {
			isReady = true;
			bReady.setText("Ready");
			bReady.setStyle("-fx-background-color: #CCCC");
			bReturn.setVisible(false);
			client.sendLobbyReadyStatus();
		}
	}

	/**
	 * Called whenever the client leaves the lobby
	 */
	public static void shutdown() {
		Client.getInstance().sendLeaveLobby();
	}

	/**
	 * @param event
	 * @throws IOException
	 */
	public void onReturn(ActionEvent event) throws IOException {
		shutdown();
		ClientMain.changeScene("Scene_MainMenu.fxml", 1280, 720);
	}

	/**
	 * Gets called by the GameUpdateThread and updates the pictures shown in the
	 * lobby
	 * 
	 * @param lobbyData which was send by the server
	 */
	public void updateLobby(byte[] lobbyData) {
		// start the game if lobby is ready
		if (lobbyData.length > 2) {
			// update the lobby scene
			ArrayList<ImageView> profiles = new ArrayList<ImageView>();
			for (int i = 0; i < lobbyData.length - 1; i++) {
				ImageView iv = new ImageView(ProfileManager.getInstance().getImage(lobbyData[i]));
				profiles.add(iv);
			}
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					// set player pictures
					horbox.getChildren().clear();
					horbox.getChildren().addAll(profiles);
				}

			});
		}
	}

}
