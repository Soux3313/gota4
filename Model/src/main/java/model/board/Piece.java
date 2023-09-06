package model.board;

import model.exceptions.UnsupportedPieceCodeException;
import model.ids.GamePlayerId;

/**
 * This enum represents a piece that can stand on a field.
 * <p>Piece have a specific pieceCode that can be drawn from this table:</p>
 * <table>
 *     <caption>PieceCodes</caption>
 * 	<tr>
 * 		<th>PieceCode</th>
 * 		<th>Meaning</th>
 * 	</tr>
 * 	<tr>
 * 		<td>0</td>
 * 		<td>PieceCode for Amazon of player 0.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>1</td>
 * 		<td>PieceCode for Amazon of player 1.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>-1</td>
 * 		<td>PieceCode for a free square. (An Empty Piece)</td>
 * 	</tr>
 * 	<tr>
 * 		<td>-2</td>
 * 		<td>A burned / blocked square.</td>
 * 	</tr>
 * </table>
 */
public enum Piece {
	AmazonPlayer1, AmazonPlayer2, Arrow, Empty;

	/**
	 * checks whether this piece is an amazon from either player
	 *
	 * @return true if it is an amazon
	 */
	public boolean isAmazon() {
		switch (this) {
			case AmazonPlayer1:
			case AmazonPlayer2:
				return true;
			case Arrow:
			case Empty:
				return false;
			default:
				throw new IllegalStateException();
		}
	}

	/**
	 * converts this piece back to the api specified piece code
	 *
	 * @return the corresponding piece code
	 */
	public int toPieceCode() {
		switch (this) {
			case AmazonPlayer1:
				return SQUARE_PLAYER0;
			case AmazonPlayer2:
				return SQUARE_PLAYER1;
			case Arrow:
				return SQUARE_ARROW;
			case Empty:
				return SQUARE_FREE;
			default:
				throw new IllegalStateException();
		}
	}

	/**
	 * constructs a piece from a piece code specified in the api
	 *
	 * @param code the piece code from which to construct a piece
	 * @return the corresponding piece
	 * @throws UnsupportedPieceCodeException if `code` is not a specified piece code
	 */
	public static Piece fromPieceCode(int code) throws UnsupportedPieceCodeException {
		switch (code) {
			case SQUARE_PLAYER0:
				return AmazonPlayer1;
			case SQUARE_PLAYER1:
				return AmazonPlayer2;
			case SQUARE_ARROW:
				return Arrow;
			case SQUARE_FREE:
				return Empty;
			default:
				throw new UnsupportedPieceCodeException("Invalid piece code", code);
		}
	}

	/**
	 * constructs a piece (specifically an amazon) from the given {@link GamePlayerId}
	 *
	 * @param id the player who should own the amazon
	 * @return the corresponding piece
	 */
	public static Piece fromPlayerId(GamePlayerId id) {
		switch (id) {
			case PLAYER1:
				return AmazonPlayer1;
			case PLAYER2:
				return AmazonPlayer2;
			default:
				throw new IllegalStateException();
		}
	}

	public Piece clone(Piece p) {
		return p;
	}

    /**
     * PieceCode for an Empty Piece
     */
    public static final int SQUARE_FREE = -1;
    /**
     * PieceCode for an Arrow
     */
    public static final int SQUARE_ARROW = -2;
    /**
     * PieceCode for a Amazon of player 0
     */
    public static final int SQUARE_PLAYER0 = 0;
    /**
     * PieceCode for a Amazon of player 1
     */
    public static final int SQUARE_PLAYER1 = 1;
}
