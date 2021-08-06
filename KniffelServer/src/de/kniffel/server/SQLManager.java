package de.kniffel.server;

import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Locale;
import java.util.Random;

import de.kniffel.server.threads.ServerRequestWorkerThread;

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
			con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
			statement = con.createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS accounts "+
									"(username CHAR(20) not NULL, "+
									"password CHAR(50) not NULL, "+
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
					"(email CHAR(50) not NULL, "+
					"pin CHAR(6) not NULL, "+
					"PRIMARY KEY (email))");
			statement.close();
			con.close();
			System.out.println("Die Datenbank 'Kniffel' und zugehörige Tabellen wurden erstellt (oder auch nicht, falls es sie schon gab)");
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public int getWins(String username) {
		try {
		Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
		String query= "SELECT wins as data FROM players WHERE username='"+username+"'";
		PreparedStatement pst = con.prepareStatement(query);
		ResultSet rs = pst.executeQuery();
		if(!rs.next()) {
			con.close();
			return 0;
		}
		int i = rs.getInt("data");
		con.close();
		return i;
		}catch(SQLException ex) {
			ex.printStackTrace();
			return 0;
		}
	}
	
	public int getLosses(String username) {
		try {
		Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
		String query= "SELECT losses as data FROM players WHERE username='"+username+"'";
		PreparedStatement pst = con.prepareStatement(query);
		ResultSet rs = pst.executeQuery();
		if(!rs.next()) {
			con.close();
			return 0;
		}
		int i = rs.getInt("data");
		con.close();
		return i;
		}catch(SQLException ex) {
			ex.printStackTrace();
			return 0;
		}
	}
	
	public int getPlayedGames(String username) {
		try {
		Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
		String query= "SELECT played as data FROM players WHERE username='"+username+"'";
		PreparedStatement pst = con.prepareStatement(query);
		ResultSet rs = pst.executeQuery();
		if(!rs.next()) {
			con.close();
			return 0;
		}
		int i = rs.getInt("data");
		con.close();
		return i;
		}catch(SQLException ex) {
			ex.printStackTrace();
			return 0;
		}
	}
	
	public int getPoints(String username) {
		try {
		Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
		String query= "SELECT points as data FROM players WHERE username='"+username+"'";
		PreparedStatement pst = con.prepareStatement(query);
		ResultSet rs = pst.executeQuery();
		if(!rs.next()) {
			con.close();
			return 0;
		}
		int i = rs.getInt("data");
		con.close();
		return i;
		}catch(SQLException ex) {
			ex.printStackTrace();
			return 0;
		}
	}
	
	public byte getProfilePic(String username) {
		System.out.println("Start "+username);
		try {
		Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
		String query= "SELECT profilepicture as prof FROM accounts WHERE username='"+username+"'";
		PreparedStatement pst = con.prepareStatement(query);
		ResultSet rs = pst.executeQuery();
		if(!rs.next()) {
			con.close();
			return 0;
		}
		int i = rs.getInt("prof");
		con.close();
		System.out.println("return "+i);
		return (byte)i;
		
		}catch(SQLException ex) {
			ex.printStackTrace();
			return 0;
		}
	}
	
	
	public void setProfilePic(String username,int id) {
		try {
			executeSQLStatement("UPDATE accounts SET profilepicture="+id+" WHERE username='"+username+"'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void addWin(String username) {
		try {
			executeSQLStatement("UPDATE players SET wins="+(getWins(username)+1)+" WHERE username='"+username+"'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addLoss(String username) {
		try {
			executeSQLStatement("UPDATE players SET losses="+(getLosses(username)+1)+" WHERE username='"+username+"'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addPlayedGame(String username) {
		try {
			executeSQLStatement("UPDATE players SET played="+(getPlayedGames(username)+1)+" WHERE username='"+username+"'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addPoints(String username, int points) {
		try {
			executeSQLStatement("UPDATE players SET points="+(getPoints(username)+points)+" WHERE username='"+username+"'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			
			executeSQLStatement("INSERT INTO accounts VALUES ('"+username+"',PASSWORD('"+password+"'),'"+email+"','"+0+"')");
			executeSQLStatement("INSERT INTO players VALUES ('"+username+"','"+0+"','"+0+"','"+0+"','"+0+"',b'0')");
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
	public String login(String username, String password, ServerRequestWorkerThread wt) {
		try {
		Connection con = DriverManager.getConnection("jdbc:mysql://"+this.url+"/KNIFFEL",this.user,this.password);
		String query= "SELECT username,password FROM accounts WHERE username='"+username+"' AND password=PASSWORD('"+password+"')";
		PreparedStatement pst = con.prepareStatement(query);
		ResultSet rs = pst.executeQuery();
		//No result
		if(!rs.next()) {
			con.close();
			return "0";
		}
		System.out.println("User "+username+ " has logged in with password "+password);
		con.close();
		return SessionManager.getInstance().addNewSession(username,wt);
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
				executeSQLStatement("UPDATE accounts SET password=PASSWORD('"+password+"') WHERE email='"+mail+"'");
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
