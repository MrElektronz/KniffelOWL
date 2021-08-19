package de.kniffel.client.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import de.kniffel.client.controllers.EndScreenController;
import de.kniffel.client.controllers.LobbyController;
import de.kniffel.client.controllers.MainMenuController;
import de.kniffel.client.controllers.OfflineGameBoardController;
import de.kniffel.client.controllers.OnlineGameBoardController;
import de.kniffel.client.gameplay.Bot.BotDifficulty;
import de.kniffel.client.serialize.OnlineSessionWrapper;
import de.kniffel.client.threads.GameUpdateThread;
import de.kniffel.client.threads.TimeoutThread;
import de.kniffel.client.utils.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * This is the main class of this project. This class contains the starting
 * point of the program and brings most of the classes together to let them
 * operate the way they should.
 * 
 * @author KBeck
 *
 */
public class ClientMain extends Application {

	private static Stage stg;
	private static TimeoutThread tThread;
	public static GameUpdateThread lThread;
	public static MainMenuController mainMenu;
	public static OnlineGameBoardController onlineGame;
	public static boolean gameEnded = false;

	@Override
	public void start(Stage primaryStage) {

		//Starts the TimeoutThread (which pings the server)
		tThread = new TimeoutThread();
		tThread.start();
		try {
			//Opens the window
			stg = primaryStage;
			primaryStage.setResizable(false);
			primaryStage.setTitle("Kniffel OWL");
			primaryStage.setOnCloseRequest(event -> {
				LobbyController.shutdown();
				Client.getInstance().logout();
				if (lThread != null) {
					lThread.setRunning(false);
				}

				tThread.setRunning(false);
			});
			//Shows the login scene
			BorderPane root = (BorderPane) FXMLLoader.load(ClientMain.class.getResource("Scene_Login.fxml"));
			Scene scene = new Scene(root, 600, 400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Changes the scene
	 * 
	 * @param fxml   Scene-file to load
	 * @param width  of the window
	 * @param height of the window
	 * @throws IOException
	 */
	public static void changeScene(String fxml, int width, int height) throws IOException {
		Parent root = FXMLLoader.load(ClientMain.class.getResource(fxml));
		stg.getScene().setRoot(root);
		stg.setWidth(width);
		stg.setHeight(height);
		stg.centerOnScreen();
	}

	/**
	 * Changes the scene
	 * 
	 * @param difficulty The bots difficulty
	 * @throws IOException
	 */
	public static void changeToOfflineScene(BotDifficulty difficulty) throws IOException {
		FXMLLoader loader = new FXMLLoader(ClientMain.class.getResource("Scene_GameBoard.fxml"));
		Parent root = (Parent) loader.load();
		OfflineGameBoardController ctrl = (OfflineGameBoardController) loader.getController();
		stg.getScene().setRoot(root);
		stg.setWidth(1280);
		stg.setHeight(745);
		stg.centerOnScreen();
		ctrl.setDifficulty(difficulty);
	}

	/**
	 * Load another scene
	 * 
	 * @param fxml load the scene from this file
	 * @throws IOException
	 */
	public static void changeScene(String fxml) throws IOException {
		Parent root = FXMLLoader.load(ClientMain.class.getResource(fxml));
		stg.getScene().setRoot(root);
	}

	/**
	 * Changes to game end screen
	 * 
	 * @param text  to display
	 * @param color of the text
	 * @throws IOException
	 */
	public static void changeToEndScreen(String text, String color) throws IOException {
		FXMLLoader loader = new FXMLLoader(ClientMain.class.getResource("Scene_End.fxml"));
		Parent root = (Parent) loader.load();
		EndScreenController ctrl = (EndScreenController) loader.getController();
		stg.getScene().setRoot(root);
		stg.setWidth(600);
		stg.setHeight(400);
		stg.centerOnScreen();
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				ctrl.setText(text, color);
			}
		});
	}

	
	/**
	 * main method
	 * 
	 * @param args handed over to the 'launch'-method
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * 
	 * @param key to read value from
	 * @return returns the value of the key 'key' stored in client.properties (next
	 *         to the .jar file) or returns "" if key was not found
	 */
	public static String readProperty(String key) {
		createFileIfNotExist();
		try (InputStream in = new FileInputStream(new File("client.properties"))) {
			Properties prop = new Properties();

			prop.load(in);
			return prop.getProperty(key);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return "";
	}

	/**
	 * updates the user and password value stored in client.properties
	 * 
	 * @param user     username
	 * @param password password
	 */
	public static void updateUserPassword(String user, String password) {
		try (InputStream in = new FileInputStream(new File("client.properties"))) {
			Properties prop = new Properties();

			prop.load(in);
			String ip = prop.getProperty("server.ip");
			String port = prop.getProperty("server.port");
			in.close();
			OutputStream output = new FileOutputStream(new File("client.properties"));
			prop.setProperty("server.ip", ip);
			prop.setProperty("server.port", port);
			prop.setProperty("client.user", user);
			prop.setProperty("client.password", password);

			prop.store(output, null);
			output.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * creates a new client.properties file if not found
	 */
	public static void createFileIfNotExist() {
		File f = new File("client.properties");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try (OutputStream output = new FileOutputStream(f)) {

				Properties prop = new Properties();

				// set the properties value
				prop.setProperty("server.ip", "localhost");
				prop.setProperty("server.port", "1337");
				prop.setProperty("client.password", "");
				prop.setProperty("client.user", "");

				// save properties to project root folder
				prop.store(output, null);

			} catch (IOException io) {
				io.printStackTrace();
			}

		}
	}

	/**
	 * Gets called every time the client receives a game update. Update the current
	 * game session
	 * 
	 * @param deserialize deserialized session send over by the server
	 */
	public static void updateSession(OnlineSessionWrapper deserialize) {
		if (gameEnded) {
			return;
		}
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				try {
					if (onlineGame == null) {
						FXMLLoader loader = new FXMLLoader(
								ClientMain.class.getResource("Scene_OnlineGameBoardController.fxml"));
						Parent root = (Parent) loader.load();
						OnlineGameBoardController ctrl = (OnlineGameBoardController) loader.getController();
						stg.getScene().setRoot(root);
						stg.setWidth(1280);
						stg.setHeight(745);
						stg.centerOnScreen();
						onlineGame = ctrl;
					}
					onlineGame.updateSession(deserialize);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	/**
	 * Gets called to end the Game
	 */
	public static void endGame() {
		if (onlineGame != null) {
			gameEnded = true;
			onlineGame.endGame();
			onlineGame = null;
		}
	}
}
