package de.kniffel.server;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Locale;
import java.util.Random;

public class SQLManager{

	private String url,user,password;
	
	public SQLManager(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}
	
	
	
	public void createDatatablesIfNotExisting() {
		Connection con = null;
		Statement statement = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/",this.user,this.password);
			statement = con.createStatement();
			statement.executeUpdate("CREATE DATABASE IF NOT EXISTS KNIFFEL");
			statement.close();
			con.close();
			con = DriverManager.getConnection("jdbc:mysql://localhost/KNIFFEL","root","");
			statement = con.createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS accounts "+
									"(username CHAR(20) not NULL, "+
									"password CHAR(32) not NULL, "+
									"email CHAR(32) not NULL, "+
									"profilepicture TINYINT not NULL, "+
									"PRIMARY KEY (username))");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS players "+
					"(username CHAR(20) not NULL, "+
					"wins INT, "+
					"losses INT, "+
					"played INT, "+
					"points INT, "+
					"achievements BIT(32), "+
					"PRIMARY KEY (username))");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS registration "+
					"(email CHAR(20) not NULL, "+
					"pin CHAR(6) not NULL, "+
					"PRIMARY KEY (email))");
			statement.close();
			con.close();
			System.out.println("Die Datenbank 'Kniffel' und zugehörige Tabellen wurden erstellt (oder auch nicht, falls es sie schon gab)");
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * @return 0 if username already exists, 1 if account was successfully created, 2 if the format doesnt match, 3 other error
	 */
	public byte createAccount(String username, String password, String email) {
		
	
		if(passStringCharTest(username) && password.length()<33 && username.length()<21){
		
		try {
			executeSQLStatement("INSERT INTO accounts VALUES ('"+username+"','"+password+"','"+email+"','"+0+"')");
			System.out.println("User "+username+ " was added to the database with password "+password);
			return 1;
		} catch (SQLException e) {
			if(e.getErrorCode() == 1062) {
			return 0;
			}
			return 3;
		}
	  }
		return 2;
    }
	/**
	 * 
	 * @param username
	 * @param password
	 * @return session-ID login successful, '0' username/password wrong, '' other error, '-1' already logged in
	 */
	public  String login(String username, String password) {
		try {
		Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
		String query= "SELECT username,password FROM accounts WHERE username='"+username+"' AND password='"+password+"'";
		PreparedStatement pst = con.prepareStatement(query);
		ResultSet rs = pst.executeQuery();
		//No result
		if(!rs.next()) {
			return "0";
		}
		System.out.println("User "+username+ " has logged in with password "+password);
		return SessionManager.addNewSession(username);
		}catch(SQLException ex) {
			return "";
			
		}
	}
	
	private void executeSQLStatement(String statement) throws SQLException {
		Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
		Statement st = con.createStatement();
		st.executeUpdate(statement);
		st.close();
		con.close();
	}
	
	/**
	 * 
	 * @return true if the parameter does not contain $,#,;,=
	 */
	private boolean passStringCharTest(String str) {
		return !(str.contains("$") || str.contains("#") || str.contains(";")||str.contains("="));
	}
	

	//The server never knows the decrypted password and only receives the encrypted one
	public String encrypt(String in) {
		try {
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(in.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1, digest);
		String hashtext = bigInt.toString(16);
		
		while(hashtext.length()<32) {
			hashtext= "0"+hashtext;
		}
		return hashtext;
		}catch(Exception e) {
			return null;
		}
		
	}

	
	

}
