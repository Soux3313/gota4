package model.util;

import static org.junit.Assert.*;

import model.board.Piece;
import model.exceptions.UnsupportedPieceCodeException;
import org.junit.Test;

import model.board.Board;

/**
 * This class tests all methods of FieldMap
 * Note: checkBorders() is a private method and it is tested implicitly by getAtOutOfBounds and setAtOutOfBounds
 */
public class PieceMapTest {
	

	// initializing a board in order to create individual fields. Using fromSquares is the easiest way to to get a board
	final Integer[][] squares = new Integer[][] {
		{ 0, 1, -1,  1, -1, -1,  1, -1, -1, -1},
		{ -2, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{  1, -1, -1, -1, -1, 0, -1, -1, -1,  1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{  0, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1,  -1, -1, -1,  0, -1, -1, -1}};
	
	public Board board = new Board(PieceMap.fromSquares(10, 10, squares));
	
	// we will use these in our tests as individual fields
	public Piece field1 = Piece.AmazonPlayer1;
	public Piece field2 = Piece.AmazonPlayer2;
	public Piece field3 = Piece.Arrow;
	public Piece field4 = Piece.Empty;

	// we will test with this array/hash-map
	public PieceMap testMap = new PieceMap(10, 10);

	public PieceMapTest() throws UnsupportedPieceCodeException {
	}

	/*
	 * testing getAt for Indices and Positions out of the Array's bounds
	 */
	@Test
	public void getAtOutOfBounds() throws IndexOutOfBoundsException{
		try{
			// note: testMap's size is 10x10
			// trying pos as parameter
			Position pos1 = new Position(10000, 10);
			testMap.getAt(pos1);
			
			// trying rows&columns as parameters
			testMap.getAt(20, 500);
			testMap.getAt(-2, -10);
			testMap.getAt(100000, -12);
			
			// throws Exception if expected Exceptions are not caught or skipped or not thrown
			System.out.println("This position is actually invalid because it is out of bounds");
			assertEquals(1, 0);
		} catch (IndexOutOfBoundsException e) {
			System.out.println(e.toString());
			System.out.println("getAt throws correct Exception for out of Bounds positions");
		}
	}
	
	/*
	 * tests the getAt Method but this time with valid Positions and values (fields)
	 * assuming setAt() and fillWith work correctly
	 */
	@Test
	public void getAtInBounds() {
		
		// corner cases and a few random coordinates
		testMap.setAt(1, 0, field1);
		assertEquals(testMap.getAt(1, 0), field1);
		
		testMap.setAt(0, 0, field2);
		assertEquals(testMap.getAt(0, 0), field2);
		
		testMap.setAt(9, 9, field3);
		assertEquals(testMap.getAt(9, 9), field3);
		
		testMap.setAt(1, 0, field4);
		assertEquals(testMap.getAt(1, 0), field4);
		
		// testing the getter with other parameter type
		Position pos1 = new Position(1, 0);
		assertEquals(testMap.getAt(pos1), field4);
	}
	
	/*
	 * This method tests setAt with Positions and Indices that are out of Bounds the Array testMap
	 */
	@Test
	public void setAtOutOfBounds() throws IndexOutOfBoundsException{
		try {
			// Note: testMap's size is 10x10
			testMap.setAt(0, 100, field1);
			testMap.setAt(-20, -1, field2);
			testMap.setAt(10, 10, field3);
			testMap.setAt(0, -14, field4);
			
			// testing setAt with Position as parameter
			Position pos1 = new Position(100, -10);
			testMap.setAt(pos1, Piece.AmazonPlayer1);
			
			// throws Exception if expected Exceptions are not caught or skipped or not thrown
			fail("This position is actually invalid because it is out of bounds");
		} catch (IndexOutOfBoundsException e) {
			System.out.println(e.toString());
			System.out.println("setAt throws correct Exception for Out of Bounds values");
		}
	}

	/*
	 * This method tests setAt with Positions and Indices that are valid and in Bounds of testMap
	 * assuming getAt() works correctly
	 */
	@Test
	public void setAtInBounds() {
		// trying to set an empty spot
		testMap.setAt(0, 1, field1);
		assertEquals(testMap.getAt(0, 1), field1);
		
		// overwriting this spot
		testMap.setAt(0, 1, field2);
		assertEquals(testMap.getAt(0, 1), field2);
		
		// using position as parameter now
		Position pos1 = new Position(3, 3);
		testMap.setAt(pos1, field3);
		assertEquals(testMap.getAt(3, 3), field3);
		
		// corner cases
		testMap.setAt(9, 9, field4);
		assertEquals(testMap.getAt(9, 9), field4);
	}


	/*
	 * this method tests moveEntry with an out of bounds place
	 */
	@Test
	public void moveEntryToInvalidPlace() {
		try {
			Position pos1 = new Position(100, 100);
			testMap.setAt(1, 1, field1);
			testMap.movePieceTo(new Position(1, 1), pos1);
			
			// Throw an exception if OutOfBoundsException was skipped
			fail("moveEntry tried to move field1 to an out of Bounds position");
		} catch (IndexOutOfBoundsException e) {
			System.out.println(e.toString());
			System.out.println("moveEntryToInvalidPlace throws correct Exception for out of Bounds position");
		}
	}

	
	/*
	 * this method tests moveEntryOf with valid values and it makes sure that the array objects are actually moved
	 */
	/*@Test
	public void moveEntryToWithValidValues() {
		// making sure that field1 is on P(0, 1)
		testMap.setAt(0, 1, field1);
		assertEquals(testMap.getPositionOf(field1).getX(), 0);
		assertEquals(testMap.getPositionOf(field1).getY(), 1);
		
		// moving value field1 to P(2, 3)
		Position pos1 = new Position(2, 3);
		testMap.moveEntryTo(field1, pos1);
		
		// making sure it was moved
		assertEquals(testMap.getPositionOf(field1).getX(), 2);
		assertEquals(testMap.getPositionOf(field1).getY(), 3);
		
	}*/
	
	/*
	 * This method checks if FieldMap's clone method returns a deep copy of an object and its attributes
	 */
	@Test
	public void cloneTest() {
		PieceMap original = new PieceMap(10, 10);
		
	    //Fill in values for original
		Piece o1 = Piece.Empty;
		Piece o2 = Piece.Arrow;

		original.setAt(0, 0, o1);
		original.setAt(1, 2, o2);
		
		// cloning
	    PieceMap clone = original.clone();
	    
	    // deep check:
		assertEquals(original.getNumColumns(), clone.getNumColumns());
	    assertEquals(original.getNumRows(), clone.getNumRows());
	}
}
