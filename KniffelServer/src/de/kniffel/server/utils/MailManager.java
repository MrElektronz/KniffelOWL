package de.kniffel.server.utils;

import java.sql.SQLException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.kniffel.server.main.ServerMain;

public class MailManager {

	private static MailManager instance;
	
	/*
	 * private constructor, so no other class can instanciate from MailManager
	 */
	private MailManager() {}
	
	/**
	 * using singleton design pattern
	 * @return one and only instance of MailManager
	 */
	public static MailManager getInstance() {
		if(instance == null) {
			instance = new MailManager();
		}
		return instance;
	}
	
	
	
	/**
	 * sends a email to the usernames mail adress to reset its password
	 * @param username
	 * @return 1 if successful
	 */
	public byte sendResetPasswordPIN(String username)
	{
		String pin = new RandomString(6).nextString();
		String email = ServerMain.getSQLManager().getMailAddress(username);
		if(email.length()>3) {
			
		//add the email with the pin to the database
		try {
			ServerMain.getSQLManager().executeSQLStatement("INSERT INTO pins VALUES ('"+email+"','"+pin+"')");
			//sending mail
			sendMail(email, pin, username);
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
	
	
	/**
	 * send message to recepient to reset his/her password
	 * @param recepient
	 * @param pin
	 * @param username
	 */
	private void sendMail(String recepient, String pin, String username) {
		Properties prop = new Properties();
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable","true");
		prop.put("mail.smtp.host", "smtp.gmail.com");
		prop.put("mail.smtp.port", "587");
		String acc = "Kniffelowl@gmail.com";
		String password = "..,|fertigistdasmondgesicht_:|";
		
		Session session = Session.getInstance(prop,new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(acc, password);
			}
		});
		Message m = prepareMessage(session,acc,recepient,pin, username);
		try {
			Transport.send(m);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param session
	 * @param acc
	 * @param recepient
	 * @param pin
	 * @param username
	 * @return Message object, with all the new account's data to send to the users email
	 */
	private Message prepareMessage(Session session, String acc, String recepient, String pin,String username) {
		try {
		Message m = new MimeMessage(session);
		m.setFrom(new InternetAddress(acc));
		m.setRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
		m.setSubject("Code to reset your password at KniffelOWL");
		m.setText("Hello "+username+",\nenter this code to successfully reset your password\n"+pin);
		return m;
		}catch(Exception ex) {
			
		}
		return null;
	}
}
