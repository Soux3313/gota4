package model.jsonstruct;
import model.ids.GamePlayerId;
import model.player.Move;
import model.player.Shot;
import model.player.Turn;
import model.util.Position;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This class tests all methods of TurnStruct-class
 */
public class TurnStructTest {
    // initializing a turn
    Move move = new Move(new Position(1, 2), new Position(3,4));
    Shot shot = new Shot(new Position(4, 5));
    Turn turn = new Turn(move, shot, GamePlayerId.PLAYER1);

    /**
     * this method tests fromModel() from TurnStruct class with an example
     */
    @Test
    public void fromModelTest(){
        // initializing teststruct from the example-turn in this testclass
        TurnStruct testStruct = TurnStruct.fromModel(turn);
        // checking values of move start
        assertEquals(testStruct.move.start.row, Integer.valueOf(1));
        assertEquals(testStruct.move.start.column, Integer.valueOf(2));
        // checking values of move end
        assertEquals(testStruct.move.end.row, Integer.valueOf(3));
        assertEquals(testStruct.move.end.column, Integer.valueOf(4));
        // checking values of shot
        assertEquals(testStruct.shot.row, Integer.valueOf(4));
        assertEquals(testStruct.shot.column, Integer.valueOf(5));
    }

    /**
     * This method tests intoModel() from TurnStruct class with an example
     * assuming fromModel() works
     */
    @Test
    public void intoModelTest(){
        // initializing teststruct from the example-turn in this testclass
        TurnStruct testStruct = TurnStruct.fromModel(turn);
        // initializing turn from teststruct
        Turn newTurn = testStruct.intoModel(turn.getPlayerId());
        // checking values of Move.start
        assertEquals(newTurn.getMove().getStart().getRow(), 1);
        assertEquals(newTurn.getMove().getStart().getColumn(), 2);
        // checking values of Move.end
        assertEquals(newTurn.getMove().getEnd().getRow(), 3);
        assertEquals(newTurn.getMove().getEnd().getColumn(), 4);
        // checking values of shot
        assertEquals(newTurn.getShot().getShotPosition().getRow(), 4);
        assertEquals(newTurn.getShot().getShotPosition().getColumn(), 5);

    }



}
