package model.jsonstruct;

import model.util.Position;
import validation.JsonRequireRecv;

/**
 * this defined the json structure of a position as specified by the api
 */
public class PositionStruct {
	@JsonRequireRecv
	public Integer row;
	@JsonRequireRecv
	public Integer column;

	public Position intoModel() {
		return new Position(row, column);
	}

	public static PositionStruct fromModel(final Position pos) {
		PositionStruct self = new PositionStruct();

		self.row = pos.getRow();
		self.column = pos.getColumn();

		return self;
	}
}