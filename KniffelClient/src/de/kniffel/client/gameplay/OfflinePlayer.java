package de.kniffel.client.gameplay;

/**
 * 
 * @author KBeck This class is the super class of bots and the player itself. It
 *         has literally no other functionality other than holding an attribute
 *         called "username". It is called "OfflinePlayer", so that it doesn't
 *         get confused for OnlinePlayers, which are handled differently.
 */
public class OfflinePlayer {

	protected String username;

	public OfflinePlayer(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
}
