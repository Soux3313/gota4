package model.board;

import static org.junit.Assert.*;

import java.util.stream.Stream;

import model.exceptions.UnsupportedPieceCodeException;
import org.junit.Test;

import model.util.Position;
/*
 * This class tests getNeighbours of the class Field
 */
public class FieldTest {

	//Initializing board for the field
	final Integer[][] squares = new Integer[][] {
		{  0, -1, -1, -2, -2, -2, -1, -1, -1,  0},
		{ -1, -1, -1, -2,  1, -2, -1, -1, -1, -1},
		{ -1, -1, -1, -2, -2, -2, -1, -1, -1, -1},
		{  1, -1, -1, -2, -2, -2, -1, -1, -1, -1},
		{ -1, -1, -1, -1,  0, -1, -1, -1, -1, -1},
		{ -1, -1, -1,  1, -1,  1, -1, -1, -1, -1},
		{  0, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1,  1,  1, -2, -1, -1, -1, -1},
		{  0, -1, -1, -2, -1,  1,  0, -1,  1, 0}};
		
	final Board board = Board.fromSquares(10, 10, squares);

	public FieldTest() throws UnsupportedPieceCodeException {
	}

	/*
	 * Testing getNeighbours by counting the neighbours of different fields
	 */
	@Test
	public void getNeighboursTest() {
		// making sure that getNeighbours returns only -2 because P(1, 4) is blocked all around
		Stream<Position> stream = board.getNeighboursOf(new Position(1, 4));
		// iterating through all neighbors of P(1, 4)
		for (Position s : (Iterable<Position>) stream::iterator) {
			assertEquals(board.getAt(s).toPieceCode(),-2);
		}
		
		//Testing a field in a corner and counting the types of neighbours
	    stream = board.getNeighboursOf(new Position(9, 9));
	    //Counts all 1
	    int count1 = 0;
	    //Counts all -1
	    int count2 = 0;
	    for (Position s : (Iterable<Position>)stream::iterator) {
			if(board.getAt(s).toPieceCode()==1) {
				count1++;
			} else if(board.getAt(s).toPieceCode()==-1) {
				count2++;
			}
			else {
				fail();
			}
		}
	    //Making sure that it counted one 1 and two -2
		assertEquals(count1,1);
		assertEquals(count2,2);
		
		//Testing a field at the edge and counting the types of neighbours
	    stream = board.getNeighboursOf(new Position(9, 4));
	    //Counts all 1
	    count1 = 0;
	    //Counts all -2
	    count2 = 0;
	    for (Position s : (Iterable<Position>) stream::iterator) {
	    	if(board.getAt(s).toPieceCode()==1) {
				count1++;
			} else if(board.getAt(s).toPieceCode()==-2) {
				count2++;
			}
			else {
				fail();
			}
		}
	    //Making sure that it counted three 1 and two -2
		assertEquals(count1,3);
		assertEquals(count2,2);
		
		//Testing a field in the middle and counting the types of neighbours
	    stream = board.getNeighboursOf(new Position(4, 4));
	    //Counts all 1
	    count1 = 0;
	   	//Counts all -2
	    count2 = 0;
	    //Counts all -1
	    int count3 = 0;
	    for (Position s : (Iterable<Position>)stream::iterator) {
	    	if(board.getAt(s).toPieceCode()==1) {
				count1++;
			} else if(board.getAt(s).toPieceCode()==-2) {
				count2++;
			} else if(board.getAt(s).toPieceCode()==-1) {
				count3++;
			}
			else {
				fail();
			}
		}
	    //Making sure that it counted three -2, three -1 and two 1
		assertEquals(count1,2);
		assertEquals(count2,3);
		assertEquals(count3,3);
	}

}
