package de.kniffel.server.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


public class ConfigManager {
	
	public static String readFromProperties(String key) {
		try (InputStream in = ConfigManager.class.getResourceAsStream("config.properties")) {
			Properties prop = new Properties();

			prop.load(in);
			return prop.getProperty(key);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return "";
	}


}
