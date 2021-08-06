package de.client.serialize;

import java.util.Arrays;

import de.client.serialize.OnlinePlayerWrapper;

public class OnlinePlayerWrapper {
	
	private String name;
	private byte profilePic;
	private int[] scores;
	public OnlinePlayerWrapper(String name, byte profilePic) {
		super();
		this.name = name;
		this.profilePic = profilePic;
		//0-5 are aces, twoes, etc. 6-12 full house and stuff
		this.scores = new int[13];
	}
	
	public String getName() {
		return name;
	}
	
	public byte getProfilePic() {
		return profilePic;
	}
	public int[] getScores() {
		return scores;
	}
	
	public void setScores(int[] scores) {
		this.scores = scores;
	}
	public void setScore(int index, int value)
	{
		scores[index] = value;
	}
	
	
	/**
	 * 
	 * @return String in format {name;profilepic;score0,score1,...,score12}
	 */
	public String serialize() {
		String s = "{";
		s+=name+";";
		s+=profilePic+";";
		for(int i = 0; i< 12;i++) {
			s+=scores[i]+",";
		}
		s+=scores[12]+"}";
		
		return s;
	}
	
	
	/**
	 * 
	 * @param data serialized OnlinePlayerWrapper object
	 * @return new OnlinePlayerWrapper object from serialized string
	 */
	public static OnlinePlayerWrapper deserialize(String data) {
		String[] args = data.replace("{", "").replace("}", "").split(";");
		if(!args[0].equals("0")) {
		OnlinePlayerWrapper playerWrapper = new OnlinePlayerWrapper(args[0], Byte.parseByte(args[1]));
		int[] scores = new int[13];
		for(int i = 0; i<scores.length;i++) {
			scores[i] = Integer.parseInt(args[2].split(",")[i]);
		}
		playerWrapper.setScores(scores);
		return playerWrapper;
		}else {
			return null;
		}
	}
	
	

}
