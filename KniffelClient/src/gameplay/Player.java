package gameplay;

public abstract class Player {

	protected String username;
	
	public Player(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
}
