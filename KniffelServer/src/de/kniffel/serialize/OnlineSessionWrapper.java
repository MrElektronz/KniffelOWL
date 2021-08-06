package de.kniffel.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * This class has all important information about an online game and gets send to the client (serialized)
 * One thought was using Gson for serialization, but we try to create our own
 * @author KBeck
 *
 */
public class OnlineSessionWrapper {
	
	private OnlinePlayerWrapper[] players;
	private int turn,currentRoll;
	private ArrayList<Integer> dice;
	private ArrayList<Integer> bank;
	
	
	public OnlineSessionWrapper(OnlinePlayerWrapper wp1,OnlinePlayerWrapper wp2,OnlinePlayerWrapper wp3,OnlinePlayerWrapper wp4) {
		players = new OnlinePlayerWrapper[] {wp1,wp2,wp3,wp4};
		this.turn = 0;
		this.currentRoll = 1;
		dice = new ArrayList<>(Arrays.asList(3,1,1,5,6));
		bank = new ArrayList<Integer>();
	}
	
	
	public void addPlayer(OnlinePlayerWrapper p) {
		for(int i = 0; i< players.length;i++) {
			if(players[i] == null) {
				players[i] = p;
				break;
			}
		}
	}
	
	public OnlinePlayerWrapper getCurrentPlayer() {
		return players[turn % getPlayerCount()];
	}
	
	public void removePlayer(String name) {
		for(int i = 0; i< players.length;i++) {
			if(players[i] != null && players[i].getName().equals(name)) {
				players[i] = null;
				break;
			}
		}
	}
	
	public int getPlayerCount() {
		int j = 0;
		for(int i = 0; i< players.length;i++) {
			if(players[i] != null) {
				j++;
			}
		}
		return j;
	}
	
	public OnlinePlayerWrapper[] getPlayers() {
		return players;
	}
	
	public void setCurrentRoll(int currentRoll) {
		this.currentRoll = currentRoll;
	}
	public void setTurn(int turn) {
		this.turn = turn;
	}
	
	public int getCurrentRoll() {
		return currentRoll;
	}
	public int getTurn() {
		return turn;
	}
	
	/**
	 * 
	 * @return ArrayList of visible 3d dice on the field
	 */
	public ArrayList<Integer> getDice() {
		return dice;
	}
	
	/**
	 * 
	 * @return ArrayList of dice on the bank
	 */
	public ArrayList<Integer> getBank() {
		return bank;
	}
	
	/**
	 * 
	 * @return ArrayList of all dice -> combining bank and 3d dice
	 */
	public ArrayList<Integer> getCombinedDice(){
		ArrayList<Integer> clone = (ArrayList<Integer>) dice.clone();
		clone.addAll(bank);
		return clone;
	}
	
	public OnlinePlayerWrapper getPlayer(int index) {
		return players[index];
	}
	
	/**
	 * 
	 * @return newly rolled dice, also stored in 'dice' arraylist
	 */
	public int[] rollDice() {
		int[] numbers = new int[dice.size()];
		dice.clear();
		Random r = new Random();
		for(int i = 0; i< numbers.length;i++) {
		int number = r.nextInt(6)+1;
		numbers[i] = number;
		dice.add(number);
		}
		currentRoll++;
		return numbers;
	}
	
	/*
	 * sets the score for the current player and adds one turn
	 */
	public void selectScore(int fieldID, int score) {
		OnlinePlayerWrapper current = getCurrentPlayer();
		current.setScore(fieldID, score);
		turn++;
		currentRoll = 1;
		}
	
	/**
	 * add specific dice with number to bank
	 */
	public void addToBank(int number) {
		if(dice.contains(number)) {
			dice.remove((Integer)number);
			bank.add(number);
		}
	}
	
	/**
	 * removes dice from bank and adds to normal field
	 * @param number
	 */
	public void removeFromBank(int number) {
		if(bank.contains(number)) {
			bank.remove((Integer)number);
			dice.add(number);
			}
	}
	
	public void setDice(ArrayList<Integer> dice) {
		this.dice = dice;
	}
	
	public void setBank(ArrayList<Integer> bank) {
		this.bank = bank;
	}

	
	/**
	 * 
	 * @return String in format {turn;currentRoll;wp1;wp2;wp3;wp4;dice1,dice2,...;bank1,bank2,...}
	 */
	public String serialize() {
		String s = "{";
		s+=turn+";";
		s+=currentRoll+";";
		
		if(players[0] != null) {
		s+=players[0].serialize()+";";
		}else {
			s+="0;";
		}
		
		if(players[1]!=null) {
		s+=players[1].serialize()+";";
		}else {
			s+="0;";
		}
		
		if(players[2]!=null) {
			s+=players[2].serialize()+";";
		}else {
			s+="0;";
		}
		
		if(players[3] != null) {
			s+=players[3].serialize()+";";
		}else {
			s+="0;";
		}
		
		for(int i = 0; i< dice.size()-1;i++) {
			s+=dice.get(i)+",";
		}
		if(dice.size()>0) {
		s+=dice.get(dice.size()-1);
		}else {
			s+="0";
		}
		
		if(bank.size()>0) {
			s+=";";
		for(int i = 0; i< bank.size()-1;i++) {
			s+=bank.get(i)+",";
		}
		s+=bank.get(bank.size()-1)+"}";
		}else {
			s+="}";
		}
		return s;
	}
	
	
	/**
	 * 
	 * @param data serialized OnlinePlayerWrapper object
	 * @return new OnlinePlayerWrapper object from serialized string
	 */
	public static OnlineSessionWrapper deserialize(String data) {
		//String[] args = data.replace("{", "").replace("}", "").split(";");
		String[] args = splitIntoArgs(data);
		OnlineSessionWrapper session = new OnlineSessionWrapper(
				OnlinePlayerWrapper.deserialize(args[2]),
				OnlinePlayerWrapper.deserialize(args[3]),
				OnlinePlayerWrapper.deserialize(args[4]),OnlinePlayerWrapper.deserialize(args[5]));
		session.setTurn(Integer.parseInt(args[0]));
		session.setCurrentRoll(Integer.parseInt(args[1]));
		String[] sDice = args[6].split(",");
		ArrayList<Integer> newDice = new ArrayList<Integer>();
		
		//Check if no dice? That's the case when sDice[0] = 0
		if(Integer.parseInt(sDice[0]) != 0) {
		for(int i = 0; i<sDice.length;i++) {
			newDice.add(Integer.parseInt(sDice[i]));
		}
		}
		session.setDice(newDice);
		if(args.length>7) {
		String[] sBank = args[7].split(",");
		ArrayList<Integer> newBank = new ArrayList<Integer>();
		for(int i = 0; i<sBank.length;i++) {
			newBank.add(Integer.parseInt(sBank[i]));
		}
		session.setBank(newBank);
		}
		return session;
	}
	
	private static String[] splitIntoArgs(String data) {
		ArrayList<String> args = new ArrayList<String>();
		data = data.substring(1,data.length()-1);
		boolean out = true;
		String between = "";
		String list = "";
		for(int i = 0; i<data.length();i++) {
			if(out) {
			if(data.charAt(i) == ';') {
				if(!list.equals("")) {
					list+=data.charAt(i-1)+",";
					args.add(list.substring(0,list.length()-1));
					list = "";
				}
				else if(data.charAt(i-1) != '}') {
					
					String next = "";
					int j = i-1;
					while(j>-1 && data.charAt(j) != '}' && data.charAt(j) != ';') {
						next+=data.charAt(j);
						j--;
					}
					args.add(new StringBuilder(next).reverse().toString());
				}
			}else if(data.charAt(i)==',') {
				list+=data.charAt(i-1)+",";
			}
			}
			if(data.charAt(i)=='{') {
				out = false;
			}
			if(!out) {
				between+=data.charAt(i);
			}
			
			if(data.charAt(i)=='}') {
				out = true;
				args.add(between);
				between = "";
			}
			
		}

		if(!list.equals("")) {
			list+=data.substring(data.length()-1);
			args.add(list);
			list = "";
		}
		if(data.charAt(data.length()-2) == ';') {
			args.add(data.charAt(data.length()-1)+"");
		}
		

		return args.toArray(new String[0]);
	}
	
}
