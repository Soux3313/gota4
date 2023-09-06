package model.jsonstruct;

import validation.JsonRequireRecvRecursive;

/**
 * defines the json structure of a move as specified by the api
 */
public class MoveStruct {
	@JsonRequireRecvRecursive
	public PositionStruct start;

	@JsonRequireRecvRecursive
	public PositionStruct end;
}
