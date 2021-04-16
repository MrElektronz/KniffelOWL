package de.kniffel.server;

import java.sql.SQLException;

public class MailManager {

	public static byte sendResetPasswordPIN(String username)
	{
		String pin = new RandomString(6).nextString();
		String email = Server.getSQLManager().getMailAdresse(username);
		if(email.length()>3) {
		//add the email with the pin to the database
		try {
			Server.getSQLManager().executeSQLStatement("INSERT INTO pins VALUES ('"+email+"','"+pin+"')");
		} catch (SQLException e) {
			if(e.getErrorCode() == 1062) {
				return 0;
				}
			e.printStackTrace();
			return 2;
		}
		if(email.equals("")) {
			return 0;
		}else if(email.equals(" ")) {
			return 2;
		}
		return 1;

		}else {
			return 0;
		}
		
	}
	
	
}
