package model.jsonstruct;

import model.board.Board;
import model.exceptions.UnsupportedPieceCodeException;
import model.util.PieceMap;
import validation.JsonRequireRecv;
import validation.JsonRequireRecvArray;

/**
 * This defines the structure of a board in Json format as specified by the api
 */
public class BoardStruct {
	@JsonRequireRecv
	public Integer gameSizeRows;
	@JsonRequireRecv
	public Integer gameSizeColumns;
	@JsonRequireRecvArray(sizes = {/*autodetect*/})
	public Integer[][] squares;

	/**
	 * Turns the BoardStruct attributes into a board
	 *
	 * @return a board with the corresponding values
	 */
	public Board intoModel() throws UnsupportedPieceCodeException {
		return new Board(PieceMap.fromSquares(gameSizeRows, gameSizeColumns, squares));
	}

	public static BoardStruct fromModel(Board b) {
		BoardStruct self = new BoardStruct();
		self.gameSizeColumns = b.getNumColumns();
		self.gameSizeRows = b.getNumRows();

		self.squares = b.toIntegerSquares();
		return self;
	}
}
