package aiplayer;

import model.board.Board;
import model.exceptions.UnsupportedPieceCodeException;
import model.ids.GamePlayerId;
import model.player.Turn;
import model.util.PieceMap;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;


/**
 * This class tests all methods of the class AIPlayerAlphaBeta.
 */
public class AIPlayerAlphaBetaTest {

    @Test
    public void bestTurnRecursiveTest() throws UnsupportedPieceCodeException {
        AIPlayerAlphaBeta ai = new AIPlayerAlphaBeta(2, new int[]{}, new float[]{1,1}, 0.8f, 1);
        Integer[][] squares = new Integer[][] {
                { -2,  1, -2, -1, -1, -1, -1, -2, -2, -2},
                { -2,  1, -2, -1, -1, -1, -1, -2, -2, -2},
                { -1, -1, -2, -2, -1, -1, -1, -2, -2, -2},
                { -2, -2, -2, -1, -1, -1, -1, -2, -2,  0},
                { -1, -2, -2, -1, -1, -1, -1, -2, -2, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -2,  1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -2, -2},
                { -2, -2, -2, -1, -1, -1, -1, -1, -1, -1},
                { -2, -1, -2, -2, -2, -2, -2, -2, -2, -2},
                {  0,  0, -1, -1, -1, -1, -1, -1, -1,  0}};
        int[][] otherQueens = {{5,9}, {0,1}, {1,0}, {1,1}};
        int[][] myQueens = {{9,9}, {9,1}, {9,0}, {2,9}};
        Turn result = ai.bestTurn(new Board(PieceMap.fromSquares(10, 10, squares)), GamePlayerId.PLAYER1, Duration.ofSeconds(10));
        //Prints the turn
        System.out.println("Start: "+result.getMove().getStart().getX()+","+result.getMove().getStart().getY()+" End: "+result.getMove().getEnd().getX()+","+result.getMove().getEnd().getY()+" Shot: "+result.getShot().getShotPosition().getX()+","+result.getShot().getShotPosition().getY());
        //Test if the right queen was chosen
        assertEquals(3,result.getMove().getStart().getX());
        assertEquals(9,result.getMove().getStart().getY());
        //Tests if the queen was moved to the right field
        assertEquals(4,result.getMove().getEnd().getX());
        assertEquals(9,result.getMove().getEnd().getY());
        //Tests if the ki shot on the right field
        assertEquals(3,result.getShot().getShotPosition().getX());
        assertEquals(9,result.getShot().getShotPosition().getY());
    }
}
