package de.kniffel.server.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import de.kniffel.server.main.GameFinder;
import de.kniffel.server.main.ServerMain;
import de.kniffel.server.utils.MailManager;
import de.kniffel.server.utils.SessionManager;

/**
 * 
 * This thread is the "core" of this project, it processes every new request a
 * client sends to a server.
 * 
 * @author KBeck
 * 
 *
 */
public class ServerRequestWorkerThread extends Thread {

	private Socket client;
	private DataInputStream in;
	private DataOutputStream out;
	private SessionManager sm;

	/**
	 * 
	 * @param client socket to client who send the message
	 */
	public ServerRequestWorkerThread(Socket client) {
		this.client = client;
		sm = SessionManager.getInstance();
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
		while (!client.isClosed()) {
			try {
				String received = "";
				try {
					received = in.readUTF();
				} catch (Exception ex) {
					client.close();
				}
				
				String[] args = received.split(";");
				String command = args[0];
				if (command.equals("$P")) {
					sm.acceptPing(args[1]);
				} else if (command.equals("$Login")) {
					String newSessionID = ServerMain.getSQLManager().login(args[1], args[2], this);
					out.writeUTF(newSessionID);
					sm.printSessions();
				} else if (command.equals("$Reg")) {
					int register = ServerMain.getSQLManager().createAccount(args[1], args[2], args[3]);
					System.out.println("User " + args[1] + " tried to register with password " + args[2]
							+ " with result: " + register);
					out.writeInt(register);
				} else if (command.equals("$Logout")) {
					// .log("AUSLOGGEN");
					sm.logout(args[1]);
					sm.printSessions();
				} else if (command.equals("$RequestResetPW")) {
					// .log("AUSLOGGEN");
					byte result = MailManager.getInstance().sendResetPasswordPIN(args[1]);
					out.writeByte(result);
				} else if (command.equals("$ResetPW")) {
					// .log("AUSLOGGEN");
					byte result = ServerMain.getSQLManager().setNewPasswordWithPin(args[1], args[2], args[3]);
					out.writeByte(result);
				} else if (command.equals("$RequestUsername")) {
					String name = sm.getSession(args[1]).getUsername();
					out.writeUTF(name);
				} else if (command.equals("$GetProf")) {
					String name = args[1];
					out.writeInt(ServerMain.getSQLManager().getProfilePic(name));
				} else if (command.equals("$SetProf")) {
					String user = sm.getSession(args[1]).getUsername();
					ServerMain.getSQLManager().setProfilePic(user, Integer.parseInt(args[2]));
				} else if (command.equals("$LobbyLeave")) {
					String sessionID = args[1];
					GameFinder.getInstance().removePlayerFromLobby(sessionID);
				} else if (command.equals("$LobbyReady")) {
					String sessionID = args[1];
					GameFinder.getInstance().setLobbyReadyStatus(sessionID);
				} else if (command.equals("$LobbyAdd")) {
					String sessionID = args[1];
					GameFinder.getInstance().addPlayerToLobby(sessionID);
				} else if (command.equals("$RequestRoll")) {
					String sessionID = args[1];
					GameFinder.getInstance().rollDice(sessionID);
				} else if (command.equals("$RequestAddBank")) {
					String sessionID = args[1];
					Integer number = Integer.parseInt(args[2]);
					GameFinder.getInstance().addDiceToBank(sessionID, number);
				} else if (command.equals("$RequestRemoveBank")) {
					String sessionID = args[1];
					Integer number = Integer.parseInt(args[2]);
					GameFinder.getInstance().removeDiceFromBank(sessionID, number);
				} else if (command.equals("$SelectScore")) {
					String sessionID = args[1];
					int scoreID = Integer.parseInt(args[2]);
					GameFinder.getInstance().setScore(sessionID, scoreID);
				}
				
				sleep(10);
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		shutdown();
	}

	/**
	 * 
	 * @return the Outputstream corresponding to the client
	 */
	public DataOutputStream getOut() {
		return out;
	}

	/**
	 * 
	 * @return socket to client who send the message
	 */
	public Socket getClient() {
		return client;
	}

	/**
	 * closes all sockets and logs the player out
	 */
	public void shutdown() {
		try {
			in.close();
			out.close();
			client.close();
			// logout if error
			sm.logout(sm.getSession(this));
			System.out.println("Close connection to CLIENT");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
