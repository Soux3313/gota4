package model.jsonstruct;

import model.ids.GamePlayerId;
import model.player.Move;
import model.player.Shot;
import model.player.Turn;
import validation.JsonRequireRecvRecursive;

/**
 * this defines the json structure of a turn as specified by the api
 */
public class TurnStruct {
	@JsonRequireRecvRecursive
	public MoveStruct move;

	@JsonRequireRecvRecursive
	public PositionStruct shot;

	public Turn intoModel(GamePlayerId id) {
		return new Turn(new Move(move.start.intoModel(), move.end.intoModel()), new Shot(shot.intoModel()), id);
	}

	public static TurnStruct fromModel(final Turn t) {
		TurnStruct self = new TurnStruct();

		self.move = new MoveStruct();
		self.move.start = PositionStruct.fromModel(t.getMove().getStart());
		self.move.end = PositionStruct.fromModel(t.getMove().getEnd());

		self.shot = PositionStruct.fromModel(t.getShot().getShotPosition());

		return self;
	}
}
