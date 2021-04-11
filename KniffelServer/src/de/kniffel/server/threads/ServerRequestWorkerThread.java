package de.kniffel.server.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
		
		
		String command = input.readUTF();
		if(command.startsWith("$P;")) {
			command = command.replace("$P;", "");
			SessionManager.acceptPing(command.split(";")[0]);
			
		}else if(command.startsWith("$Login;")) {
			command = command.replace("$Login;", "");
			System.out.println("Server: Tries to login user with username "+command.split(";")[0]+" and password "+command.split(";")[1]);
			String newSessionID = Server.getSQLManager().login(command.split(";")[0], command.split(";")[1]);
			System.out.println("Result: "+newSessionID);
			System.out.println("now sends sessionID");
			out.writeUTF(newSessionID);
			SessionManager.printSessions();
		}else if(command.startsWith("$Reg;")) {
			command = command.replace("$Reg;", "");
			String[] args = command.split(";");
			int register = Server.getSQLManager().createAccount(args[0], args[1], args[2]);
			System.out.println("User "+args[0]+" tried to register with password "+args[1]+" with result: "+register);
			out.writeInt(register);
		}else if(command.startsWith("$Logout;")) {
			//.log("AUSLOGGEN");
			command = command.replace("$Logout;", "");
			SessionManager.logout(command);
			SessionManager.printSessions();
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

}
