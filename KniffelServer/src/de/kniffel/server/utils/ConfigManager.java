package de.kniffel.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ConfigManager {
	
	/**
	 * 
	 * @param key
	 * @return value stored in the config file under the key
	 */
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
