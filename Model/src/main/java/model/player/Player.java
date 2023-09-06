package model.player;

import model.ids.GlobalPlayerId;

/**
 * This class represents a player in the game. Each player has a playerId, a
 * name and url of the webservice the player runs his game at.
 *
 *
 */
public class Player {

	/**
	 * The player ID
	 */
	private GlobalPlayerId playerId;

	/**
	 * The name of the player
	 */
	private String name;

	/**
	 * The URL of the player's web service
	 */
	private String url;

	/**
	 * Constructor of the Player.
	 * @param playerId the id of the Player.
	 * @param name the name of the Player.
	 * @param url the url of the Player.
	 */
	public Player(GlobalPlayerId playerId, String name, String url) {
		this.playerId = playerId;
		this.name = name;
		this.url = url;
	}

	@Override
	public String toString() {
		return "PlayerId: " + this.getPlayerId() + ", name:" + this.getName() + ", url: " + this.getUrl();
	}

	/**
	 * Getter for the playerId.
	 * @return the playerId.
	 */
	public GlobalPlayerId getPlayerId() {
		return this.playerId;
	}

	/**
	 * Setter for the playerId.
	 * @param playerId the playerId to set.
	 */
	public void setPlayerId(GlobalPlayerId playerId) {
		this.playerId = playerId;
	}

	/**
	 * Getter for the player's name.
	 * @return the player's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for  the player's name.
	 * @param name  the player's name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for the player's url.
	 * @return the player's url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Setter for  the player's url.
	 * @param url  the player's url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public Player clone() {
		return new Player(this.playerId.clone(), name, url);
	}
}
