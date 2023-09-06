package model.board;

import model.exceptions.InvalidTurnException;
import model.exceptions.UnsupportedPieceCodeException;
import model.player.Move;
import model.player.Shot;
import model.player.Turn;
import model.ids.GamePlayerId;
import model.util.PieceMap;
import model.util.Position;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 * This class tests all methods of the Board class
 *
 * 
 */
public class BoardTest {
	
	// The format to address this is int[row][column], with [0][0] being at the top left corner.
	// Defining this here to avoid retyping this several times, a generic int-array with all piececodes appearing in it
	// 0 player 0
	// 1 player 1
	// -1 empty
	// -2 arrow
	final Integer[][] testSquares = new Integer[][] {
		{ 0, 1, -1,  1, -1, -1,  1, -1, -1, -1},
		{ -1, -1, -1, -1, -2, -1, -1, -1, -1, -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{  1, -1, -1, -1, -1, 0, -1, -1, -1,  1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{  0, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1,  -1, -1, -1,  0, -1, -1, -1}};

	@Test
	public void testGetAllPositions() {
		Board b = new Board(2, 3);
		Position[] actual = b.getAllValidPositions().toArray(Position[]::new);

		Position[] expected = new Position[] {
				new Position(0, 0),
				new Position(0, 1),
				new Position(0, 2),
				new Position(1, 0),
				new Position(1, 1),
				new Position(1, 2)
		};

		assertArrayEquals(expected, actual);
	}

	/*
	 * This method tests the constructor of class Board. This should not cause any Exceptions.
	 */
	@Test
	public void testConstructor() {
		// initializing Board with Field-TwoWayArray
		PieceMap testMap = new PieceMap(10, 10);
		Board board = new Board(testMap);
		
		// test is trivial but just making sure...
		assertEquals(board.getNumRows(), 10);
		assertEquals(board.getNumColumns(), 10);
	}
	
	/*
	 * this method tests static method fromSquares of board class with a variety of values
	 * assuming getFieldAt() works
	 */
	@Test
	public void testFromSquaresValidSquares() throws UnsupportedPieceCodeException {
		// initializing a board with static method fromSquares
		Board board = Board.fromSquares(10, 10, testSquares);
		
		// test is trivial but just making sure...
		assertEquals(board.getNumRows(), 10);
		assertEquals(board.getNumColumns(), 10);
		
		// P(3, 5) has Player 0 with Piece Code 0 on it, and so on
		// testing all Piececodes
		assertEquals(board.getAt(3, 5).toPieceCode(), 0);
		assertEquals(board.getAt(2, 2).toPieceCode(), -1);
		assertEquals(board.getAt(1, 4).toPieceCode(), -2);
		assertEquals(board.getAt(0, 1).toPieceCode(), 1);
		
		// corner cases
		assertEquals(board.getAt(0, 0).toPieceCode(), 0);
		assertEquals(board.getAt(0, 9).toPieceCode(), -1);
		assertEquals(board.getAt(9, 0).toPieceCode(), -1);
		assertEquals(board.getAt(9, 9).toPieceCode(), -1);
		
	}
	
	/*
	 * this method tests static method fromSquares of board class with invalid row&column size
	 */
	@Test
	public void testFromSquaresInvalidSize() {
			
		try {
			Board weirdBoard = Board.fromSquares(65, 3, testSquares);
			// throws exception if Assert was skipped
			fail("Exception was skipped");
			} catch(AssertionError | UnsupportedPieceCodeException e) {
				System.out.println(e.toString());
				System.out.println("constructor of Board throws correct Exception for invalid board sizes");
			}
	}
	
	/*
	 * this method tests static method fromSquares of board class with an invalid piece-code
	 */
	@Test
	public void testFromSquaresInvalidConfig() {
		// this squares has an invalid Piececode at [5][9]
		Integer[][] invalidSquares = new Integer[][] {
				{ 0, 1, -1,  1, -1, -1,  1, -1, -1, -1},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
				{  1, -1, -1, -1, -1, 0, -1, -1, -1,  1},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -100},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
				{  0, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
				{ -1, -1, -1,  -1, -1, -1,  0, -1, -1, -1}};
		try {
			Board board = Board.fromSquares(10, 10, invalidSquares);
			// throws exception if IllegalArgumentException was skipped
			fail("Exception was skipped");
		} catch (IllegalArgumentException | UnsupportedPieceCodeException e) {
			System.out.println(e.toString());
			System.out.println("constructor of Board throws correct Exception for invalid Piececode");
		}
	}
	
	/*
	 * this method tests toSquares() method from board with a valid Board full of pieces
	 * assuming fromSquares() and getFieldAt() work correctly
	 */
	@Test
	public void testToSquaresValidBoard() throws UnsupportedPieceCodeException {
		Board board = Board.fromSquares(10, 10, testSquares);
		int[][] squares = board.toSquares();
		
		// P(3, 5) has Piece-Code 0
		assertEquals(squares[3][5], board.getAt(3, 5).toPieceCode());
		// P(0, 1) has Piece-Code 1
		assertEquals(squares[0][1], board.getAt(0, 1).toPieceCode());
		// P(2, 2) has Piece-Code -1
		assertEquals(squares[2][2], board.getAt(2, 2).toPieceCode());
		// P(1, 4) has Piece-Code -2
		assertEquals(squares[1][4], board.getAt(1, 4).toPieceCode());
		
		// corner cases
		assertEquals(squares[0][0], board.getAt(0, 0).toPieceCode());
		assertEquals(squares[0][9], board.getAt(0, 9).toPieceCode());
		assertEquals(squares[9][0], board.getAt(9, 0).toPieceCode());
		assertEquals(squares[9][9], board.getAt(9, 9).toPieceCode());
	}
	
	/*
	 * this method tests isOutOfRange() method from Board class with a variety of inBound and out of Bound values
	 */
	@Test
	public void testIsOutOfRange() throws UnsupportedPieceCodeException {
		// Note: the board we test on is has size 10x10
		Board board = Board.fromSquares(10, 10, testSquares);
		
		// In-Bound values
		assertFalse(board.isOutOfBounds(new Position(1, 3)));
		assertFalse(board.isOutOfBounds(new Position(5, 7)));
		assertFalse(board.isOutOfBounds(new Position(8, 8)));
		
		// Corner cases
		assertFalse(board.isOutOfBounds(new Position(0, 0)));
		assertFalse(board.isOutOfBounds(new Position(9, 9)));

		assertTrue(board.isOutOfBounds(new Position(10, 10)));
		assertTrue(board.isOutOfBounds(new Position(-1, -1)));
		
		// out of Bound values
		assertTrue(board.isOutOfBounds(new Position(100, 1)));
		assertTrue(board.isOutOfBounds(new Position(1, 100)));
		assertTrue(board.isOutOfBounds(new Position(1000, 1000)));
		assertTrue(board.isOutOfBounds(new Position(-10, 6)));
	}

	/*
	 * this method tests isValid() in Board class
	 */
	@Test
	public void testIsValid() throws UnsupportedPieceCodeException {
		// The format to address this is int[row][column], with [0][0] being at the top left corner.
		Integer[][] squares = new Integer[][] {
			{ -1, -1, -1,  1, -1, -1,  1, -1, -1, -1}, //0
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //1
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //2
			{  1, -1, -1, -1, -1, -1, -1, -1, -1,  1}, //3
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //4
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //5
			{  0, -1, -1, -1, -1, -1, -2, -1, -1,  0}, //6
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //7
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //8
			{ -1, -1, -1,  0, -1, -1,  0, -1, -1, -1}};//9
			// 0   1   2   3   4   5   6   7   8   9
		Board board = Board.fromSquares(10,10,squares);
		//Valid moves
		//Tests basic move
		Turn valid_turn = new Turn(new Move(new Position(9,6),new Position(9,8)),new Shot(new Position(8,8)), GamePlayerId.PLAYER1);
		assertTrue(board.isValid(valid_turn));
		//Test shooting at old spot
		valid_turn = new Turn(new Move(new Position(9,6),new Position(9,8)),new Shot(new Position(9,6)),GamePlayerId.PLAYER1);
		assertTrue(board.isValid(valid_turn));
		//Test diagonal movement/shot
		valid_turn = new Turn(new Move(new Position(0,3),new Position(3,6)),new Shot(new Position(6,3)),GamePlayerId.PLAYER2);
		assertTrue(board.isValid(valid_turn));
		//Test movement to 0/0 and 9/9
		valid_turn = new Turn(new Move(new Position(9,6),new Position(9,9)),new Shot(new Position(9,6)),GamePlayerId.PLAYER1);
		assertTrue(board.isValid(valid_turn));
		valid_turn = new Turn(new Move(new Position(0,3),new Position(0,0)),new Shot(new Position(0,3)),GamePlayerId.PLAYER2);
		assertTrue(board.isValid(valid_turn));
		//Invalid moves
		//Move out of range
		Turn invalid_turn = new Turn(new Move(new Position(9,6),new Position(9,10)),new Shot(new Position(9,6)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		invalid_turn = new Turn(new Move(new Position(0,3),new Position(0,-3)),new Shot(new Position(0,3)),GamePlayerId.PLAYER2);
		assertFalse(board.isValid(invalid_turn));
		//No piece at start
		invalid_turn = new Turn(new Move(new Position(0,4),new Position(0,5)),new Shot(new Position(0,4)),GamePlayerId.PLAYER2);
		assertFalse(board.isValid(invalid_turn));
		//Move blocked by self
		invalid_turn = new Turn(new Move(new Position(0,3),new Position(0,6)),new Shot(new Position(0,4)),GamePlayerId.PLAYER2);
		assertFalse(board.isValid(invalid_turn));
		invalid_turn = new Turn(new Move(new Position(0,3),new Position(0,7)),new Shot(new Position(0,4)),GamePlayerId.PLAYER2);
		assertFalse(board.isValid(invalid_turn));
		//Move blocked by arrow
		invalid_turn = new Turn(new Move(new Position(9,6),new Position(6,6)),new Shot(new Position(7,6)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		invalid_turn = new Turn(new Move(new Position(9,6),new Position(5,6)),new Shot(new Position(7,6)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		//Move blocked by other
		invalid_turn = new Turn(new Move(new Position(6,0),new Position(3,0)),new Shot(new Position(5,0)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		invalid_turn = new Turn(new Move(new Position(6,0),new Position(2,0)),new Shot(new Position(5,0)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		//Arrow blocked
		invalid_turn = new Turn(new Move(new Position(6,0),new Position(6,1)),new Shot(new Position(6,6)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		invalid_turn = new Turn(new Move(new Position(6,0),new Position(6,1)),new Shot(new Position(6,7)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		//Shoot myself (accurate description of my emotion writing these tests)
		invalid_turn = new Turn(new Move(new Position(6,0),new Position(6,1)),new Shot(new Position(6,1)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		invalid_turn = new Turn(new Move(new Position(6,0),new Position(6,3)),new Shot(new Position(9,3)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		//No movement
		invalid_turn = new Turn(new Move(new Position(6,0),new Position(6,0)),new Shot(new Position(6,1)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		//Move non diagonally
		invalid_turn = new Turn(new Move(new Position(6,0),new Position(3,4)),new Shot(new Position(3,3)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		//Shoot non diagonally
		invalid_turn = new Turn(new Move(new Position(6,0),new Position(3,3)),new Shot(new Position(6,1)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
		//Try to move other
		invalid_turn = new Turn(new Move(new Position(6,0),new Position(3,3)),new Shot(new Position(6,0)),GamePlayerId.PLAYER2);
		assertFalse(board.isValid(invalid_turn));
		invalid_turn = new Turn(new Move(new Position(0,6),new Position(3,3)),new Shot(new Position(5,1)),GamePlayerId.PLAYER1);
		assertFalse(board.isValid(invalid_turn));
	}
	
	/*
	 * this method tests getAmazons() in Board class by iterating through its output stream with a small example
	 * assuming fromSquares and toPieceCode works
	 */
	@Test
	public void testGetAmazonsWith8Amazons() throws UnsupportedPieceCodeException {
		// using a small board for simplicity
		Integer[][] squares = new Integer[][] {
			{ 1, -1, -2, 1},
			{-1, 1, 1, -1},
			{-1, 0, 0, -1},
			{0, -2, -1, 0}};
		// initializing small board
		Board board = Board.fromSquares(4, 4, squares);
		Stream<Position> stream = board.getAmazonPositions();
		
		// iterating through all elements of output amazon-stream
		// counting amazons
		int amazon_count = 0;
		int player1_count = 0;
		int player0_count = 0;
		
		for (Position a : (Iterable<Position>) stream::iterator) {
			// System.out.println(a.toPieceCode());

			int pc = board.getAt(a).toPieceCode();
			assertTrue(pc == 1 || pc == 0);
			amazon_count++;
			if (pc == 0) {
				player0_count++;
			} else {
				player1_count++;
			}
		}
		
		// making sure all amazons were found
		assertEquals(amazon_count, 8);
		assertEquals(player0_count, 4);
		assertEquals(player1_count, 4);
	}	
	
	/*
	 * this method tests getAmazons() with a board without amazons
	 * assuming fromSquares works correctly
	 */
	@Test
	public void testGetAmazonsNoAmazons() throws UnsupportedPieceCodeException {
		Integer[][] squares = new Integer[][] {
		// this board will have no players
			{ -1, -1, -2, -1},
			{-1, -1, -1, -1},
			{-1, -1, -1, -1},
			{-1, -2, -1, -1}};
		Board board = Board.fromSquares(4, 4, squares);
		
		// making sure stream has no values
		Stream<Position> stream = board.getAmazonPositions();
		assertFalse(stream.findAny().isPresent());
	}

	/*
	 * This method tests isTurnPossible() by Board class
	 */
	@Test
	public void testIsTurnPossible() throws UnsupportedPieceCodeException {
		// freeSquares allows player0 and 1 to move
		final Integer[][] freeSquares = new Integer[][] {
			{ -1, 1, 0,  -1, -1, 0,  1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, 0, -1, -1, 0, -1, -1, -1, -1},
			{  -1, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{  -1, 1, -1, 1, -1, -1, 1, -1, 1,  -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1,  -1, -1, -1,  0, -1, -1, -1}};
		
		//Player player0 = new Player(0, "Hans", "omfgdogs.com");
		//Player player1 = new Player(1, "Peter", "omfgdogs.com");
		
		Board freeBoard = Board.fromSquares(10, 10, freeSquares);
		
		// freeBoard has enough spaces for both players to move
		assertTrue(freeBoard.isTurnPossible(GamePlayerId.PLAYER1));
		assertTrue(freeBoard.isTurnPossible(GamePlayerId.PLAYER2));
		
		// Creating a Board that restricts player1 with arrows while player0 has one move left
		final Integer[][] restrictedByArrowSquares = new Integer[][] {
			{ -2, -2, 0,  -2, -2, 0,  -2, -2, -2, -2},
			{ -2, -2, -2, -2, -2, -1, -2, -2, -2, -2},
			{ -2, -2, 0, -2, -2, -2, -2, -2, -2, -2},
			{  -2, -2, -2, -2, 0, -2, -2, -2, -2,  -2},
			{ -2, -2, -2, -2, -2, -2, -2, -2, -2, -2},
			{ -2, -2, -2, -2, -2, -2, -2, -2, -2, -2},
			{  -2, 1, -2, 1, -2, -2, 1, -2, 1,  -2},
			{ -2, -2, -2, -2, -2, -2, -2, -2, -2, -2},
			{ -2, -2, -2, -2, -2, -2, -2, -2, -2, -2},
			{ -2, -2, -2,  -2, -2, -2,  -2, -2, -2, -2}};
			
		Board restrictedByArrowsBoard = Board.fromSquares(10, 10, restrictedByArrowSquares);
		
		// player0 has one square left to go while player1 is surrounded by hazards
		assertTrue(restrictedByArrowsBoard.isTurnPossible(GamePlayerId.PLAYER1));
		assertFalse(restrictedByArrowsBoard.isTurnPossible(GamePlayerId.PLAYER2));
		
		// player0 blocks the way for player1 and has one turn more to go
		final Integer[][] restrictedByPlayerSquares = new Integer[][] {
			{ -2, -2, -2,  -2, -2, 0,  -2, -2, -2, -2},
			{ -2, -2, -2, -2, -2, -2, -2, -2, -2, -2},
			{ -2, -2, 0, -2, -2, -2, -2, -2, -2, -2},
			{  -2, -2, -2, -2, 0, -2, -2, -2, -2,  -2},
			{ -2, -2, -2, -2, -2, -2, -2, -1, -2, -2},
			{ -2, -2, -2, -2, -2, -2, -2, 0, -2, -2},
			{  -2, 1, -2, 1, -2, -2, 1, -2, 1,  -2},
			{ -2, -2, -2, -2, -2, -2, -2, -2, -2, -2},
			{ -2, -2, -2, -2, -2, -2, -2, -2, -2, -2},
			{ -2, -2, -2,  -2, -2, -2,  -2, -2, -2, -2}};
			
		Board restrictedByPlayerBoard = Board.fromSquares(10, 10, restrictedByPlayerSquares);
		
		// player0 has one square left to go while player1 is surrounded by hazards and an enemy
		assertTrue(restrictedByPlayerBoard.isTurnPossible(GamePlayerId.PLAYER1));
		assertFalse(restrictedByPlayerBoard.isTurnPossible(GamePlayerId.PLAYER2));
		
		// no one should be able to move in this board
		final Integer[][] restricted = new Integer[][] {
			{ -2, -2, -2,  -2, -2, 0,  -2, -2, -2, -2},
			{ -2, -2, -2, -2, -2, -2, -2, -2, -2, -2},
			{ -2, -2, 0, -2, -2, -2, -2, -2, -2, -2},
			{  -2, -2, -2, -2, 0, -2, -2, -2, -2,  -2},
			{ -2, -2, -2, -2, -2, -2, -2, -2, -2, -2},
			{ -2, -2, -2, -2, -2, -2, -2, 0, -2, -2},
			{  -2, 1, -2, 1, -2, -2, 1, -2, 1,  -2},
			{ -2, -2, -2, -2, -2, -2, -2, -2, -2, -2},
			{ -2, -2, -2, -2, -2, -2, -2, -2, -2, -2},
			{ -2, -2, -2,  -2, -2, -2,  -2, -2, -2, -2}};
		
		Board restrictedBoard = Board.fromSquares(10, 10, restricted);
		
		// both players have no way of moving
		assertFalse(restrictedBoard.isTurnPossible(GamePlayerId.PLAYER1));
		assertFalse(restrictedBoard.isTurnPossible(GamePlayerId.PLAYER2));

		final Integer[][] testing = new Integer[][]{
				{-2,-2,-2,-1,-2,-2,-2, 0,-2,-2},
				{-2,-1,-2,-2,-2,-2,-2,-2,-2, 1},
				{-1,-2,-2,-2, 1,-2,-2,-2,-2,-2},
				{-2,-2,-2,-2,-2,-2,-2,-2,-2,-2},
				{-2,-2,-2,-2,-2,-2,-1,-1,-2,-2},
				{-2,-2,-2,-2, 1,-2,-1,-2,-1,-2},
				{ 0,-2,-1,-2, 0,-2,-2,-2,-2,-2},
				{-2,-2,-2,-2,-2,-2,-1,-2,-2,-2},
				{-1,-2,-2,-2,-2,-2,-2,-1,-2,-2},
				{-2,-2, 0,-2,-1,-2,-2,-2,-2, 1}};
		Board testingBoard = Board.fromSquares(10, 10, testing);
		assertFalse(testingBoard.isTurnPossible(GamePlayerId.PLAYER1));
	}
	
	/*
	 * This method tests the method isClearPath in Board class with clear paths
	 */
	@Test
	public void testIsPathClearFreeSquares() throws UnsupportedPieceCodeException {
		
		// we will test P(0, 0) with P(0, 2) first, formally P(y1, x1) and P(y2, x2)
		Position p1 = new Position(0,0);
		Position p2 = new Position(0, 2);
		
		// in this case the Path is free as P(0, 1) and P(0, 2) are free
		final Integer[][] clearSquares = new Integer[][] {
			{ 0, -1, -1,  1, -1, -1,  1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{  1, -1, -1, -1, -1, 0, -1, -1, -1,  1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{  0, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1,  -1, -1, -1,  0, -1, -1, -1}};
			
		Board clearBoard = Board.fromSquares(10,10, clearSquares);
		
		assertTrue(clearBoard.isPathClear(p1, p2));
		
		// now we will test diagonally, P(0, 0) with P(2, 2)
		Position p3 = new Position(2, 2);
		// the path between 0,0 and 2,2 is clear. There are no entities
		final Integer[][] clearDiagonalSquares = new Integer[][] {
			{ 0, -1, -1,  -1, -1, -1,  1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{  1, -1, -1, -1, -1, 0, -1, -1, -1,  1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{  0, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1,  -1, -1, -1,  0, -1, -1, -1}};
					
		Board clearDiagonalBoard = Board.fromSquares(10,10, clearDiagonalSquares);
			
		assertTrue(clearDiagonalBoard.isPathClear(p1, p3));
	}
	
	/*
	 * this method tests isPathClear in board class with Positions obstructed by another player or arrow
	 */
	@Test
	public void testIsPathClearObstructed() throws UnsupportedPieceCodeException {
		// now we will test P(0, 0) with P(0, 2) again
		Position p1 = new Position(0,0);
		Position p2 = new Position(0, 2);
		// but in this case the path is not free, as P(0, 2) is obstructed by a player
		final Integer[][] obstructedByPlayerSquares = new Integer[][] {
			{ 0, -1, 1,  1, -1, -1,  1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{  1, -1, -1, -1, -1, 0, -1, -1, -1,  1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{  0, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1,  -1, -1, -1,  0, -1, -1, -1}};
			
		
		Board obstructedByPlayerBoard = Board.fromSquares(10, 10, obstructedByPlayerSquares);
		assertFalse(obstructedByPlayerBoard.isPathClear(p1, p2));
		
		// same as above but this time the path is obstructed by an arrow
		final Integer[][] obstructedByArrowSquares = new Integer[][] {
			{ 0, -1, -2,  1, -1, -1,  1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{  1, -1, -1, -1, -1, 0, -1, -1, -1,  1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{  0, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1,  -1, -1, -1,  0, -1, -1, -1}};
			
		Board obstructedByArrowBoard = Board.fromSquares(10, 10, obstructedByArrowSquares);
		assertFalse(obstructedByArrowBoard.isPathClear(p1, p2));
		
		// now we will test diagonally, P(0, 0) with P(2, 2)
		Position p3 = new Position(2, 2);
		// the way is obstructed as there is a Player on P(2,2)
		final Integer[][] obstructedDiagonalSquares = new Integer[][] {
			{ 0, -1, -1,  -1, -1, -1,  1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, 1, -1, -1, -1, -1, -1, -1, -1},
			{  1, -1, -1, -1, -1, 0, -1, -1, -1,  1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{  0, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{ -1, -1, -1,  -1, -1, -1,  0, -1, -1, -1}};
					
		Board obstructedDiagonalBoard = Board.fromSquares(10,10, obstructedDiagonalSquares);
		assertFalse(obstructedDiagonalBoard.isPathClear(p1, p3));
		
	}
	@Test
	public void testApplyTurn() throws InvalidTurnException, UnsupportedPieceCodeException {
		Integer[][] squares = new Integer[][] {
				{ -1, -1, -1,  1, -1, -1,  1, -1, -1, -1}, //0
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //1
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //2
				{  1, -1, -1, -1, -1, -1, -1, -1, -1,  1}, //3
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //4
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //5
				{  0, -1, -1, -1, -1, -1, -2, -1, -1,  0}, //6
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //7
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //8
				{ -1, -1, -1,  0, -1, -1,  0, -1, -1, -1}};//9
				// 0   1   2   3   4   5   6   7   8   9
		Board board = Board.fromSquares(10,10,squares);
		// test invalid move
		Turn invalid_turn = new Turn(new Move(new Position(9,6),new Position(6,6)),new Shot(new Position(7,6)),GamePlayerId.PLAYER1);
		try{
			board.applyTurn(invalid_turn);
			fail("this should not have happened");
		} catch(InvalidTurnException e){
			//pass
		}
		//try a valid move
		Turn valid_turn = new Turn(new Move(new Position(9,6),new Position(9,8)),new Shot(new Position(8,8)), GamePlayerId.PLAYER1);
		board.applyTurn(valid_turn);
		Move move = valid_turn.getMove();
		Shot shot = valid_turn.getShot();
		assertEquals(board.getAt(move.getStart()).toPieceCode(), -1);
		assertEquals(board.getAt(move.getEnd()).toPieceCode(), 0);
		assertEquals(board.getAt(shot.getShotPosition()).toPieceCode(), -2);

	}

}
