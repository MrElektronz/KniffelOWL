package de.kniffel.server.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import org.omg.CORBA.portable.OutputStream;

import de.kniffel.serialize.Serializer;
import de.kniffel.server.GameFinder;
import de.kniffel.server.MailManager;
import de.kniffel.server.SQLManager;
import de.kniffel.server.Server;
import de.kniffel.server.SessionManager;

public class ServerRequestWorkerThread extends Thread{
	
	
	private Socket client;
	private DataInputStream in;
	private DataOutputStream out;
	private GameFinder gameFinder;
	private SessionManager sm;
	public ServerRequestWorkerThread(Socket client) {
		this.client = client;
		sm = SessionManager.getInstance();
		gameFinder = GameFinder.getInstance();
		try {
			in = new DataInputStream(client.getInputStream());
			out = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void run() {
		while(!client.isClosed()) {
		try {
			String received = "";
			try {
				received = in.readUTF();
			}catch(Exception ex) {
				client.close();
			}
		String[] args = received.split(";");
		System.out.println(received);
		String command = args[0];
		if(command.equals("$P")) {
			sm.acceptPing(args[1]);
		}else if(command.equals("$Login")) {
			System.out.println("Server: Tries to login user with username "+args[1]+" and password "+args[2]);
			String newSessionID = Server.getSQLManager().login(args[1], args[2],this);
			System.out.println("Result: "+newSessionID);
			System.out.println("now sends sessionID");
			out.writeUTF(newSessionID);
			sm.printSessions();
		}else if(command.equals("$Reg")) {
			int register = Server.getSQLManager().createAccount(args[1], args[2], args[3]);
			System.out.println("User "+args[1]+" tried to register with password "+args[2]+" with result: "+register);
			out.writeInt(register);
		}else if(command.equals("$Logout")) {
			//.log("AUSLOGGEN");
			sm.logout(args[1]);
			sm.printSessions();
		}else if(command.equals("$RequestResetPW")) {
			//.log("AUSLOGGEN");
			byte result = MailManager.getInstance().sendResetPasswordPIN(args[1]);
			out.writeByte(result);
		}else if(command.equals("$ResetPW")) {
			//.log("AUSLOGGEN");
			byte result = Server.getSQLManager().setNewPasswordWithPin(args[1], args[2], args[3]);
			out.writeByte(result);
		}else if(command.equals("$ReqUsername")) {
			String name = sm.getSession(args[1]).getUsername();
			out.writeUTF(name);
		}else if(command.equals("$GetProf")) {
			String name = args[1];
			System.out.println("GETPROFFFFFFFFFFFFF");
			out.writeInt(Server.getSQLManager().getProfilePic(name));
		}else if(command.equals("$SetProf")) {
			//SetProf;sessionid;imageID
			String user = sm.getSession(args[1]).getUsername();
			Server.getSQLManager().setProfilePic(user, Integer.parseInt(args[2]));
		}else if(command.equals("$LobbyUpdate")) {
			//SetProf;sessionid;imageID
			//This is depricated
			String sessionID = args[1];
			//writeInts(out,GameFinder.getInstance().updateLobby(sessionID));
		}else if(command.equals("$LobbyLeave")) {
			String sessionID = args[1];
			System.out.println("LEAAAAAAAAAAAVE "+sessionID);
			GameFinder.getInstance().removePlayerFromLobby(sessionID);
		}else if(command.equals("$LobbyReady")) {
			String sessionID = args[1];
			GameFinder.getInstance().setLobbyReadyStatus(sessionID);
		}else if(command.equals("$LobbyAdd")) {
			String sessionID = args[1];
			//Sending the client serialized lobby information
			//out.writeUTF("!LobbyUpdate;"+Serializer.toString();
			//out.flush();
			GameFinder.getInstance().addPlayerToLobby(sessionID);
			
		}
		//System.out.println("Server: "+input.readUTF()+" by "+client.getRemoteSocketAddress());
		
		sleep(10);
		}catch(IOException ex) {
			ex.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
		try {
		in.close();
		out.close();
		client.close();
		//logout if error
		sm.logout(sm.getSession(this));
		System.out.println("Close connection TO CLIENT");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DataOutputStream getOut() {
		return out;
	}
	
	public Socket getClient() {
		return client;
	}
	

    
}
