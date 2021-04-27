package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import client.Client;
import client.TimeoutThread;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application {

	private static Stage stg;
	private static TimeoutThread tThread;

	@Override
	public void start(Stage primaryStage) {
		// Client.register("Emre2", "passwort2", "xaxaxa@gmail.com");
		tThread = new TimeoutThread();
		tThread.start();
		try {
			stg = primaryStage;
			primaryStage.setResizable(false);
			primaryStage.setTitle("Kevins & Kevins kooles Kniffelspiel");
			primaryStage.setOnCloseRequest(event -> {
				Client.logout();
				tThread.setRunning(false);
			});
			BorderPane root = (BorderPane) FXMLLoader.load(Main.class.getResource("Scene_Login.fxml"));
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
	 * @param fxml
	 * @throws IOException
	 */
	public static void changeScene(String fxml, int width, int height) throws IOException {
		Parent root = FXMLLoader.load(Main.class.getResource(fxml));
		stg.getScene().setRoot(root);
		stg.setWidth(width);
		stg.setHeight(height);
		stg.centerOnScreen();
	}
	
	public static void changeScene(String fxml) throws IOException {
		Parent root = FXMLLoader.load(Main.class.getResource(fxml));
		stg.getScene().setRoot(root);
	}

	/**
	 * main method
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * 
	 * @param key
	 * @return returns the value of the key 'key' stored in client.properties (next to the .jar file) or returns "" if key was not found
	 */
	public static String readProperty(String key) {
		createFileIfNotExists();
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
	 * @param user
	 * @param password
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
	public static void createFileIfNotExists() {
		File f = new File("client.properties");
		if(!f.exists()) {
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
}
