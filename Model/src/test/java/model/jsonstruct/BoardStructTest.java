package model.jsonstruct;

import model.board.Board;
import model.exceptions.UnsupportedPieceCodeException;
import model.util.PieceMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This class tests all methods of class Board Struct
 */
public class BoardStructTest {

    // Board that will be turned into boardStruct, small in order to make it more readable
    // assuming fromSquares() in Board class works
    final Integer[][] testSquares = new Integer[][] {
            {1, -1},
            {-1, -2},
            {0, 0}
    };
    // initializing Board
    final Board board = new Board(PieceMap.fromSquares(3, 2, testSquares));

    public BoardStructTest() throws UnsupportedPieceCodeException {
    }

    /**
     * This method tests fromModel() in BoardStruct-class with an example
     */
    @Test
    public void fromModelTest() {
        // initializing BoardStruct
        BoardStruct testBoardStruct = BoardStruct.fromModel(board);

        // check if struct values are correct
        assertEquals(testBoardStruct.gameSizeColumns, Integer.valueOf(2));
        assertEquals(testBoardStruct.gameSizeRows, Integer.valueOf(3));

        assertEquals(testBoardStruct.squares[0][0], Integer.valueOf(1));
        assertEquals(testBoardStruct.squares[1][0], Integer.valueOf(-1));
        assertEquals(testBoardStruct.squares[2][1], Integer.valueOf(0));
        assertEquals(testBoardStruct.squares[1][1], Integer.valueOf(-2));
    }

    /**
     * This method tests intoModel() in BoardStruct-class with an example
     * Assuming fromModel() in Board class works
     */
    @Test
    public void intoModelTest() {
        // initializing BoardStruct
        BoardStruct testBoardStruct = BoardStruct.fromModel(board);
        // debug purposes
        System.out.println(testBoardStruct.gameSizeColumns);
        System.out.println(testBoardStruct.gameSizeRows);
        // creating a Board from BoardStruct
        // auto-generated try/catch
        Board newBoard = null;
        try {
            newBoard = testBoardStruct.intoModel();
        } catch (UnsupportedPieceCodeException e) {
            e.printStackTrace();
        }

        // check if Board's values are correct
        assert newBoard != null;
        assertEquals(newBoard.getNumColumns(), 2);
        assertEquals(newBoard.getNumRows(), 3);

        assertEquals(newBoard.getAt(0, 0).toPieceCode(), 1);
        assertEquals(newBoard.getAt(1, 0).toPieceCode(), -1);
        assertEquals(newBoard.getAt(2, 1).toPieceCode(), 0);
        assertEquals(newBoard.getAt(1,1).toPieceCode(), -2);
    }
}
