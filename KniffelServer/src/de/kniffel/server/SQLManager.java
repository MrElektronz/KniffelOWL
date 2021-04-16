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
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS pins "+
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
	 * @return 0 if username already exists, 1 if account was successfully created, 2 if the format doesnt match, 3 other error, 4 email already in use
	 */
	public byte createAccount(String username, String password, String email) {
		
		if(passStringCharTest(username) && password.length()<33 && username.length()<21 && username.length()>2 && password.length()>1){
			
		try {
			
			Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
			String query= "SELECT COUNT(*) AS total FROM accounts WHERE email='"+email+"'";
			PreparedStatement pst = con.prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			if(rs.next() &&rs.getInt("total") > 0) {
				//email already in use
				con.close();
				return 4;
			}
			
			executeSQLStatement("INSERT INTO accounts VALUES ('"+username+"','"+password+"','"+email+"','"+0+"')");
			System.out.println("User "+username+ " was added to the database with password "+password);
			return 1;
		} catch (SQLException e) {
			if(e.getErrorCode() == 1062) {
			return 0;
			}
			System.out.println("error: " +e.getErrorCode());
			e.printStackTrace();
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
	public String login(String username, String password) {
		try {
		Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
		String query= "SELECT username,password FROM accounts WHERE username='"+username+"' AND password='"+password+"'";
		PreparedStatement pst = con.prepareStatement(query);
		ResultSet rs = pst.executeQuery();
		//No result
		if(!rs.next()) {
			con.close();
			return "0";
		}
		System.out.println("User "+username+ " has logged in with password "+password);
		con.close();
		return SessionManager.addNewSession(username);
		}catch(SQLException ex) {
			return "";
			
		}
	}
	
	public void executeSQLStatement(String statement) throws SQLException {
		Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
		Statement st = con.createStatement();
		st.executeUpdate(statement);
		st.close();
		con.close();
	}
	
	/**
	 * 
	 * @return true if the parameter does not contain $,#,;,*
	 */
	private boolean passStringCharTest(String str) {
		return !(str.contains("$") || str.contains("#") || str.contains(";")||str.contains("=")||str.contains("*"));
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
	
	public String getMailAdresse(String username) {
		try {
		Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
		String query= "SELECT email as mail FROM accounts WHERE username='"+username+"'";
		PreparedStatement pst = con.prepareStatement(query);
		ResultSet rs = pst.executeQuery();
		if(!rs.next()) {
			con.close();
			return "";
		}
		return rs.getString("mail");
		
		}catch(SQLException ex) {
			ex.printStackTrace();
			return " ";
		}
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @param pin
	 * @return 1 if password was reset, 0 if username not found, 2 if other error, 3 if pin wrong
	 */
	
	public byte setNewPasswordWithPin(String username, String password, String pin) {
		//There was no valid password entered/encrypted
		if(password.length()<31 || username.length() <2) {
			return 2;
		}else if(pin.length() != 6) {
			return 3;
		}
		String mail = getMailAdresse(username);
		
		try {
			Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
			String query= "SELECT pin as code FROM pins WHERE email='"+mail+"'";
			PreparedStatement pst = con.prepareStatement(query);
			ResultSet rs = pst.executeQuery();
			if(!rs.next()) {
				con.close();
				return 0;
			}
			
			String code = rs.getString("code");
			if(pin.equals(code)) {
				//Set new password if entered pin is correct
				executeSQLStatement("UPDATE accounts SET password='"+password+"' WHERE email='"+mail+"'");
				//Remove row from pins table
				executeSQLStatement("DELETE FROM pins WHERE email='"+mail+"'");
				
				return 1;
			}else {
				return 3;
			}
			
			}catch(SQLException ex) {
				ex.printStackTrace();
				return 2;
			}
		
	}
	

	


}
