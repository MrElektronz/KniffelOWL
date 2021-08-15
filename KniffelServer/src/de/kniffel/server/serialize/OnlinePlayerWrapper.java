package de.kniffel.server.serialize;

/**
 * 
 * @author KBeck
 * 
 *         This class holds all important data about an OnlinePlayer and gets
 *         serialized by an OnlineSessionWrapper to be send to a client.
 *
 */
public class OnlinePlayerWrapper {

	private String name;
	private byte profilePic;
	private int[] scores;

	/**
	 * 
	 * @param name
	 * @param profilePic
	 */
	public OnlinePlayerWrapper(String name, byte profilePic) {
		super();
		this.name = name;
		this.profilePic = profilePic;
		// 0-5 are aces, twoes, etc. 6-12 full house and stuff
		this.scores = new int[13];
	}

	/**
	 * 
	 * @return player's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return player's profile picture id
	 */
	public byte getProfilePic() {
		return profilePic;
	}

	/**
	 * 
	 * @return all scores of the player (does not include scores like "total")
	 */
	public int[] getScores() {
		return scores;
	}

	/**
	 * 
	 * @param scores new scores array
	 */
	public void setScores(int[] scores) {
		this.scores = scores;
	}

	/**
	 * 
	 * @param index to be selected
	 * @param value to be set for the index's field
	 */
	public void setScore(int index, int value) {
		scores[index] = value;
	}

	/**
	 * 
	 * @return String in format {name;profilepic;score0,score1,...,score12}
	 */
	public String serialize() {
		String s = "{";
		s += name + ";";
		s += profilePic + ";";
		for (int i = 0; i < 12; i++) {
			s += scores[i] + ",";
		}
		s += scores[12] + "}";

		return s;
	}

	/**
	 * 
	 * @param data serialized OnlinePlayerWrapper object
	 * @return new OnlinePlayerWrapper object from serialized string
	 */
	public static OnlinePlayerWrapper deserialize(String data) {
		String[] args = data.replace("{", "").replace("}", "").split(";");
		if (!args[0].equals("0")) {
			OnlinePlayerWrapper playerWrapper = new OnlinePlayerWrapper(args[0], Byte.parseByte(args[1]));
			int[] scores = new int[13];
			for (int i = 0; i < scores.length; i++) {
				scores[i] = Integer.parseInt(args[2].split(",")[i]);
			}
			playerWrapper.setScores(scores);
			return playerWrapper;
		} else {
			return null;
		}
	}

}
