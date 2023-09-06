package model.player;

import model.game.Game;
import model.ids.GamePlayerId;
import model.ids.TurnId;

/**
 * This class represents a Turn a {@link Player} made.
 * A Turn contains a {@link Move} of an Amazon and the position of a shot.
 *
 */
public class Turn {

	/**
	 * The {@link Move} of the Amazon.
	 */
	private final Move move;

	/**
	 * The {@link Shot} of the Amazon.
	 */
	private final Shot shot;

	/**
	 * The {@link Player} who made this Turn.
	 */
	private GamePlayerId playerId;

	/**
	 * The id of this turn.
	 */
	private TurnId id;

	/**
	 * Constructor of a Turn.
	 * @param m the Move the Player made.
	 * @param s the Shot the Player made.
	 */
	public Turn(Move m, Shot s, GamePlayerId id) {
		this.move = m;
		this.shot = s;
		this.playerId = id;
	}

	public Move getMove() {
		return move;
	}

	public Shot getShot() {
		return shot;
	}

	public TurnId getId() {
		return id;
	}

	public void setId(TurnId id) {
		this.id = id;
	}

	public GamePlayerId getPlayerId() {
		return this.playerId;
	}

	public void setPlayerId(GamePlayerId pid) {
		this.playerId = pid;
	}


	public Turn clone() {
		Turn t = new Turn(move.clone(), shot.clone(), playerId);
		t.setId(this.id.clone());
		return t;
	}

	@Override
	public String toString() {
		return String.format("Turn{ move: %s, shot: %s, playerId: %d }", move, shot, playerId.get());
	}
}
