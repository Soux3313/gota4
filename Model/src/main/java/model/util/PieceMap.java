package model.util;

import model.board.Piece;
import model.exceptions.UnsupportedPieceCodeException;
import validation.util.ArrayUtil;

import java.util.*;
import java.util.stream.Stream;

/**
 * convenience wrapper for a 2d array of pieces which represents the current board state
 * using this wrapper is a guarantee that the array is not jagged and provides
 * convenience methods to inspect and manipulate the underlying array in a invariance protecting manner
 */
public class PieceMap implements Cloneable, Iterable<Piece> {

	private final int numRows;
	private final int numColumns;
	private final Piece[][] pieces;

	/**
	 * constructs a FieldMap with the specified sizes
	 *
	 * @param numRows the number of rows on the map
	 * @param numColumns the number of columns on the map
	 */
	public PieceMap(int numRows, int numColumns) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		pieces = new Piece[numRows][numColumns];

		for (Piece[] inner : pieces) {
			Arrays.fill(inner, Piece.Empty);
		}
	}

	/**
	 * copy ctor
	 * @param f the field map to clone from
	 */
	public PieceMap(PieceMap f) {
		this.numRows = f.numRows;
		this.numColumns = f.numColumns;
		this.pieces = new Piece[numRows][numColumns];

		for (int i = 0; i < numRows; ++i) {
			for (int j = 0; j < numColumns; ++j) {
				this.setAt(i, j, f.getAt(i, j));
			}
		}
	}

	/**
	 * constructs a PieceMap from a Integer[][] filled with piece codes
	 *
	 * @param numRows the number of rows squares has where `rows` refers to the size of the 0th dimension
	 * @param numColumns the number of columns squares has where `columns` refers to the size of the 1st dimension
	 * @param squares the array containing valid piece codes
	 * @return a PieceMap repesenting that array
	 * @throws UnsupportedPieceCodeException if the array contains invalid piece codes
	 */
	public static PieceMap fromSquares(int numRows, int numColumns, Integer[][] squares) throws UnsupportedPieceCodeException {
		int[] actualSizes = ArrayUtil.getNormalizedDimSizes(squares);
		assert numRows <= actualSizes[0];
		assert numColumns <= actualSizes[1];

		//Construct FieldMap:
		PieceMap map = new PieceMap(numRows, numColumns);
		for(int i = 0; i < numRows; i++) {
			for(int j = 0; j < numColumns; j++) {
				map.setAt(new Position(i,j), Piece.fromPieceCode(squares[i][j]));
			}
		}
		return map;
	}

	/**
	 * constructs a PieceMap from a int[][] filled with piece codes
	 *
	 * @param numRows the number of rows squares has where `rows` refers to the size of the 0th dimension
	 * @param numColumns the number of columns squares has where `columns` refers to the size of the 1st dimension
	 * @param squares the array containing valid piece codes
	 * @return a PieceMap repesenting that array
	 * @throws UnsupportedPieceCodeException if the array contains invalid piece codes
	 */
	public static PieceMap fromSquares(int numRows, int numColumns, int[][] squares) throws UnsupportedPieceCodeException {
		// cannot check dim sizes because int[][] does not have an object base type

		//Construct FieldMap:
		PieceMap map = new PieceMap(numRows, numColumns);
		for(int i = 0; i < numRows; i++) {
			for(int j = 0; j < numColumns; j++) {
				map.setAt(new Position(i,j), Piece.fromPieceCode(squares[i][j]));
			}
		}
		return map;
	}

	/**
	 * Creates an Integer[][] from the this FieldMap containing the pieceCode of the pieces on the different fields
	 * @return the Integer[][] of pieceCodes
	 */
	public int[][] toSquares() {
		int[][] squares = new int[numRows][numColumns];
		for(int i = 0; i < numRows; i++) {
			for(int j = 0; j < numColumns; j++) {
				squares[i][j] = getAt(i,j).toPieceCode();
			}
		}
		return squares;
	}

	/**
	 * Creates an Integer[][] from the this FieldMap containing the pieceCode of the pieces on the different fields
	 * @return the Integer[][] of pieceCodes
	 */
	public Integer[][] toIntegerSquares() {
		Integer[][] squares = new Integer[numRows][numColumns];
		for(int i = 0; i < numRows; i++) {
			for(int j = 0; j < numColumns; j++) {
				squares[i][j] = getAt(i,j).toPieceCode();
			}
		}
		return squares;
	}

	/**
	 * @return the number of rows in the array
	 */
	public int getNumRows() {
		return this.numRows;
	}

	/**
	 * @return the number of columns in the array
	 */
	public int getNumColumns() {
		return this.numColumns;
	}

	/**
	 * @return  all the pieces as Iterator.
	 */
	@Override
	public Iterator<Piece> iterator() {
		return this.stream().iterator();
	}

	/**
	 * @return all the Pieces as Stream.
	 */
	public Stream<Piece> stream() {
		return Arrays.stream(this.pieces)
				.flatMap(Arrays::stream);
	}


	/**
	 * This method is used to get the Element of the Array at the given {@link Position}.
	 * @param p the Position of the Array to return the value from.
	 * @return the Element at the given Position of the foreword Array
	 *
	 * @throws IndexOutOfBoundsException if Position is out of bounds
	 */
	public Piece getAt(Position p) throws IndexOutOfBoundsException {
		return this.pieces[p.getRow()][p.getColumn()];
	}

	/**
	 * convenience method if you don't already have a position object
	 * see doc of FieldMap.getAt(Position)
	 */
	public Piece getAt(int row, int column) throws IndexOutOfBoundsException {
		return this.getAt(new Position(row, column));
	}


	/**
	 * Puts the piece `fm` at the position `p`
	 *
	 * @param p the Position to set the Element to.
	 * @param fm the Element to set at the given Position.
	 */
	public void setAt(Position p, Piece fm) throws IndexOutOfBoundsException {
		this.pieces[p.getRow()][p.getColumn()] = fm;
	}

	public void setAt(int row, int column, Piece fm) throws IndexOutOfBoundsException, IllegalStateException {
		this.setAt(new Position(row, column), fm);
	}

	/**
	 * This method is used to set the {@link Position} of an Element in the {@link #pieces} Array to a new Position.
	 * @param to the new Position the Element t should get
	 * @param from the position of the element whos position should be changed
	 */
	public void movePieceTo(Position from, Position to) {
		if (this.getAt(to) != Piece.Empty) {
			throw new IllegalArgumentException("moved to field is not empty");
		}

		Piece tmp = this.getAt(from);
		this.setAt(from, Piece.Empty);
		this.setAt(to, tmp);
	}

	/**
	 * Creates a deep clone of this FieldMap.
	 * @return a deep clone of this FieldMap.
	 */
	@Override
	public PieceMap clone() {
		return new PieceMap(this);
	}
}
