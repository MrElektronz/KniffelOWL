package de.kniffel.server.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.omg.CORBA.portable.OutputStream;

import de.kniffel.server.GameFinder;
import de.kniffel.server.MailManager;
import de.kniffel.server.SQLManager;
import de.kniffel.server.Server;
import de.kniffel.server.SessionManager;

public class ServerRequestWorkerThread extends Thread{
	
	
	private Socket client;
	
	public ServerRequestWorkerThread(Socket client) {
		this.client = client;
	}
	
	
	
	@Override
	public void run() {
		try {
		DataInputStream input = new DataInputStream(client.getInputStream());
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		
		
		String received = input.readUTF();
		String[] args = received.split(";");
		System.out.println(received);
		String command = args[0];
		if(command.equals("$P")) {
			SessionManager.acceptPing(args[1]);
		}else if(command.equals("$Login")) {
			System.out.println("Server: Tries to login user with username "+args[1]+" and password "+args[2]);
			String newSessionID = Server.getSQLManager().login(args[1], args[2]);
			System.out.println("Result: "+newSessionID);
			System.out.println("now sends sessionID");
			out.writeUTF(newSessionID);
			SessionManager.printSessions();
		}else if(command.equals("$Reg")) {
			int register = Server.getSQLManager().createAccount(args[1], args[2], args[3]);
			System.out.println("User "+args[1]+" tried to register with password "+args[2]+" with result: "+register);
			out.writeInt(register);
		}else if(command.equals("$Logout")) {
			//.log("AUSLOGGEN");
			SessionManager.logout(args[1]);
			SessionManager.printSessions();
		}else if(command.equals("$RequestResetPW")) {
			//.log("AUSLOGGEN");
			byte result = MailManager.sendResetPasswordPIN(args[1]);
			out.writeByte(result);
		}else if(command.equals("$ResetPW")) {
			//.log("AUSLOGGEN");
			byte result = Server.getSQLManager().setNewPasswordWithPin(args[1], args[2], args[3]);
			out.writeByte(result);
		}else if(command.equals("$ReqUsername")) {
			String name = SessionManager.getSession(args[1]).getUsername();
			out.writeUTF(name);
		}else if(command.equals("$GetProf")) {
			String name = args[1];
			System.out.println("GETPROFFFFFFFFFFFFF");
			out.writeInt(Server.getSQLManager().getProfilePic(name));
		}else if(command.equals("$SetProf")) {
			//SetProf;sessionid;imageID
			String user = SessionManager.getSession(args[1]).getUsername();
			Server.getSQLManager().setProfilePic(user, Integer.parseInt(args[2]));
		}else if(command.equals("$LobbyUpdate")) {
			//SetProf;sessionid;imageID
			String sessionID = args[1];
			writeInts(out,GameFinder.updateLobby(sessionID));
		}else if(command.equals("$LobbyLeave")) {
			String sessionID = args[1];
			GameFinder.removePlayerFromLobby(sessionID);
		}
		//System.out.println("Server: "+input.readUTF()+" by "+client.getRemoteSocketAddress());
		input.close();
		out.close();
		client.close();
		
		sleep(10);
		}catch(IOException ex) {
			ex.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper function to write an int[] to an output stream (taken from https://stackoverflow.com/questions/35527516/java-sending-receiving-int-array-via-socket-to-from-a-program-coded-in-c)
	 * @param out
	 * @param ints
	 * @throws IOException
	 */
    private static void writeInts(DataOutputStream dataOut, int[] ints) throws IOException {
        dataOut.writeInt(ints.length);
        for (int e : ints) dataOut.writeInt(e);
        dataOut.flush();
    }
    
}
