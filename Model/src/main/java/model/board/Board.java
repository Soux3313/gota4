package model.board;

import model.exceptions.InvalidTurnException;
import model.exceptions.UnsupportedPieceCodeException;
import model.ids.GamePlayerId;
import model.ids.TurnId;
import model.player.Move;
import model.player.Player;
import model.player.Shot;
import model.player.Turn;
import model.util.PieceMap;
import model.util.Position;
import model.util.Vec2i;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * <p>This class represents a board in the Amazon Game.
 * Each board can have a specific size (in most cases this will be 10x10).</p>
 * The board has an {@link PieceMap} to store all the fields of this board and within these the pieces
 * and their position currently in the game. Thus the board knows the current position of all pieces
 * and can therefore decide if a piece can move to to specific position or not. Look at {@link #isValid} for this.
 * The board can also find out if a player can't move at all.
 *
 */
public class Board extends PieceMap {

	private final ArrayList<Turn> appliedTurns;

	/***
	 * constructs a board from a `FieldMap`
	 * @param init the array to construct `this` from, precondition: the array must be completely filled
	 */
	public Board(PieceMap init) {
		super(init);
		this.appliedTurns = new ArrayList<>();
	}

	public Board(int numRows, int numColumns) {
		super(numRows, numColumns);
		this.appliedTurns = new ArrayList<>();
	}

	public Board(PieceMap init, Stream<Turn> appliedTurns) {
		super(init);
		this.appliedTurns = appliedTurns.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * constructs a board from a 2d {@link Integer} array filled with the piece codes
	 * specified by the api
	 *
	 * @param rows the number of rows in `squares` (where a row is indexed by the first index)
	 * @param columns the number of columns in `squares` (where a column is indexed by the second index)
	 * @param squares the array that contains the piece codes, this may not be a jagged array
	 * @return a valid board representing the same state as `squares`
	 * @throws UnsupportedPieceCodeException if the array contains any piece code not specified by the api
	 */
	public static Board fromSquares(int rows, int columns, Integer[][] squares) throws UnsupportedPieceCodeException {
		return new Board(PieceMap.fromSquares(rows, columns, squares));
	}

	/**
	 * constructs a board from a 2d `int` array filled with the piece codes
	 * specified by the api
	 *
	 * @param rows the number of rows in `squares` (where a row is indexed by the first index)
	 * @param columns the number of columns in `squares` (where a column is indexed by the second index)
	 * @param squares the array that contains the piece codes, this may not be a jagged array
	 * @return a valid board representing the same state as `squares`
	 * @throws UnsupportedPieceCodeException if the array contains any piece code not specified by the api
	 */
	public static Board fromSquares(int rows, int columns, int[][] squares) throws UnsupportedPieceCodeException {
		return new Board(PieceMap.fromSquares(rows, columns, squares));
	}

	/**
	 * @return a {@link Stream} of the turns that are applied to this board
	 */
	public Stream<Turn> getAppliedTurns() {
		return this.appliedTurns.stream();
	}

	/**
	 * @return the last turn that was applied to this board (if it exists)
	 */
	public Optional<Turn> getLastTurn() {
		if (this.appliedTurns.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(this.appliedTurns.get(this.appliedTurns.size() - 1));
	}

	/**
	 * @return the next valid/free turn id for this board
	 */
	public TurnId getNextTurnId() {
		return new TurnId(this.appliedTurns.size());
	}


	/**
	 * generates every int pair from `startInclusive` to `endExclusive`
	 * this is useful for indexing into `this`
	 *
	 * this is roughly equivalent to the following list comprehensions
	 *
	 * [(x, y) | x <- [startx..(endx-1)], y <- [starty..(endy-1)]]
	 * [(x, y) for x in range(startx, endx) for y in range(starty, endy)]
	 *
	 * @param startInclusive the pair (startx, starty) this will be contained in the sequence
	 * @param endExclusive the pair (endx, endy) this will not be contained in the sequence
	 * @return the calculated sequence
	 */
	public Stream<Vec2i> generateIntPairs(Vec2i startInclusive, Vec2i endExclusive) {
		return IntStream.range(startInclusive.getX(), endExclusive.getX())
				.boxed()
				.flatMap(r -> (
						IntStream.range(startInclusive.getY(), endExclusive.getY())
								.mapToObj(c -> new Vec2i(r, c))
				));
	}

	/**
	 * @return a {@link Stream} of every valid row-column-pair, so (0,0), (0,1), (0,2)...(1,0)...
	 */
	public Stream<Position> getAllValidPositions() {
		return generateIntPairs(new Vec2i(0, 0), new Vec2i(this.getNumRows(), this.getNumColumns()))
				.map(Position::new);
	}

	/**
	 * This method creates an {@link Stream} containing all amazon positions on this board.
	 * @return a stream containing all Amazons on the board.
	 */
	public Stream<Position> getAmazonPositions() {
		return this.getAllValidPositions()
				.filter(p -> this.getAt(p).isAmazon());
	}


	/**
	 * Checks if there is an unobstructed path on the given board between two
	 * {@link Position}.
	 *
	 * @param start   the start position of the path
	 * @param end     the end position of the path
	 * @return true if the path is unobstructed, false otherwise
	 */
	public boolean isPathClear(Position start, Position end) {

		// return false if any of the position objects is out of range
		if (isOutOfBounds(start) || isOutOfBounds(end)) {
			return false;
		}

		//return false if end position is blocked
		if(this.getAt(end) != Piece.Empty) {
			return false;
		}

		// Return false if the path is neither diagonal nor orthogonal
		if (!start.isDiagonalTo(end) && !start.isOrthogonalTo(end)) {
			return false;
		}

		// Calculate the direction of the move
		Vec2i direction = new Vec2i(
				(int) Math.signum(end.getRow() - start.getRow()),
				(int) Math.signum(end.getColumn() - start.getColumn())
		);

		// Start looking for obstacles one square away from the start
		Position p = start.moveBy(direction);

		// Search every square from the start point to the end point for an obstacle
		while (!p.equals(end)) {
			// Return false if an obstruction was found
			if (this.getAt(p) != Piece.Empty) {
				return false;
			}
			// Go one square further in the previously calculated direction
			p = p.moveBy(direction);
		}
		// Return true if a path between (y1, x1) and (y2, x2) is not obstructed
		return true;
	}

	/**
	 * <p>Checks if a player's turn is valid.</p>
	 * <p>A Turn is considered valid if both the {@link Move} and the {@link Shot}
	 * are valid.</p>
	 * <p>A move is considered valid if the player moves his own amazon on a clear
	 * path either diagonal or horizontal or vertically.</p>
	 * <p>A shot is considered valid if the Amazon ov the Move  shots an arrow from the position of the moved Amazon
	 * to an unobstructed square on a clear path either diagonal or horizontal or vertically.</p>
	 *
	 * @param turn the turn the player made
	 * @return true if a players move is valid, false if not
	 */
	public boolean isValid(Turn turn) {
		GamePlayerId movingPlayer = turn.getPlayerId();

		if (!isTurnPossible(movingPlayer)) {
			return false;
		}

		Move move = turn.getMove();
		Shot shot = turn.getShot();

		// return false if any position is out of bounds
		if (isOutOfBounds(move.getStart()) || isOutOfBounds(move.getEnd()) || isOutOfBounds(shot.getShotPosition())) {
			return false;
		}

		// return false if the path is not clear or the amazon did not move at all
		// see: 'zugzwang' in the rules
		if (move.getStart().equals(move.getEnd()) || !isPathClear(move.getStart(), move.getEnd())) {
			return false;
		}

		// Check if the player is trying to move his own piece
		Piece p = this.getAt(move.getStart());

		// Return false if the player tries to move a piece not belonging to them or
		// is not an amazon
		if (!p.isAmazon() || p != Piece.fromPlayerId(movingPlayer)) {
			return false;
		}

		// So the move of the amazon is valid
		// question remains if the shot is valid:

		// Copy the current board to a temporary board to avoid making changes until
		// after the move is valid
		Board tempBoard = this.clone();

		// move the amazon
		tempBoard.movePieceTo(move.getStart(), move.getEnd());

		// Return true after the move as well as the shot move to valid positions
		return !move.getEnd().equals(shot.getShotPosition())
				&& tempBoard.isPathClear(move.getEnd(), shot.getShotPosition());
	}

	public boolean canPieceMove(Position p) {
		return this.getNeighboursOf(p)
				.anyMatch(v -> this.getAt(v) == Piece.Empty);
	}

	/**
	 * Tests if a {@link Position} is outside the board.
	 *
	 * @param p the position to test
	 * @return true, if position is out of range, false if position is within the boards range
	 */
	public boolean isOutOfBounds(Position p) {
		return p.getRow() < 0
				|| p.getRow() >= this.getNumRows()
				|| p.getColumn() < 0
				|| p.getColumn() >= this.getNumColumns();
	}

	/**
	 * generates a {@link Stream} that contains all the valid positions (so non-out-of-bounds {@link Position}s
	 * directly next to a given {@link Position}
	 *
	 * @param p the base {@link Position} of which to determine the neighbours
	 * @return a valid stream containing the neighbours
	 */
	public Stream<Position> getNeighboursOf(Position p) {
		return generateIntPairs(new Vec2i(-1, -1), new Vec2i(2, 2))
				.filter(v -> !v.equals(new Vec2i(0, 0)))
				.map(p::moveBy)
				.filter(v -> !isOutOfBounds(v));
	}

	/**
	 * This method checks if the {@link Player} with the given {@link GamePlayerId} can make a {@link Turn}.
	 *
	 * @param id the player to check
	 * @return true if any of the player's pieces has a free square around them,
	 *         false otherwise
	 */
	public boolean isTurnPossible(GamePlayerId id) {
		return this.getAmazonPositions()
				.filter(p -> this.getAt(p) == Piece.fromPlayerId(id))
				.anyMatch(this::canPieceMove);
	}

	/**
	 * Applies a {@link Turn} to the board if possible.
	 * 
	 * @param turn the turn to apply
	 * @throws InvalidTurnException if one of the following goes wrong:
	 * 		- There is no {@link Player} assigned to the turn
	 * 		- The assigned player has a bad id
	 * 		- The move on the board is not valid. Look at {@link #isValid(Turn)}
	 */
	public void applyTurn(Turn turn) throws InvalidTurnException {
		if (!isValid(turn)) {
			throw new InvalidTurnException("Invalid move or shot");
		}

		// apply changes to the board:
		Move move = turn.getMove();
		Shot shot = turn.getShot();

		//Get the amazon that is moved:
		this.movePieceTo(move.getStart(), move.getEnd());
		this.setAt(shot.getShotPosition(), Piece.Arrow);

		turn.setId(new TurnId(this.appliedTurns.size()));
		this.appliedTurns.add(turn);
	}

	@Override
	public Board clone() {
		return new Board(super.clone(), this.appliedTurns.stream().map(Turn::clone));
	}
}