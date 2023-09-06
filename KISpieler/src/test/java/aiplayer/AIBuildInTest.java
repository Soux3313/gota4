package aiplayer;
import model.board.Board;
import model.ids.GamePlayerId;
import model.player.Turn;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;

public class AIBuildInTest {

    @Test
    public  void getFreeQueensTest() {
        int[][] squares = new int[][] {
                { -2, -2,  0, -2, -1, -1, -1,  1, -2, -2},
                { -2, -2,  1, -2, -1, -1, -1, -1, -2, -2},
                { -2, -1, -1, -2, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -2, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -1, -1, -1, -2, -1, -1},
                {  0, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -2, -2, -1, -1, -1},
                { -1, -2, -1, -1, -1, -1, -2, -2, -2,  0},
                { -2, -1, -1, -1,  0, -1, -1, -1, -2, -2},
                {  0, -1, -1, -1, -1, -1, -1, -1, -2,  1}};
        AIPlayerGreedy aiGreedy = new AIPlayerGreedy(3, new float[]{100, 10, 1}, 0.5f,1);
        AIBuildIn aiplayer = new AIBuildIn(3, new float[]{100, 10, 1}, 0.5f,1, aiGreedy);
        int[][] queens = AIPlayerGreedy.getQueensOfPlayer(squares, GamePlayerId.PLAYER2);
        int[][] freeQueens = aiplayer.getFreeQueens(squares, queens);
		for (int[] freeQueen : freeQueens) {
			System.out.println(freeQueen[0] + " , " + freeQueen[1]);
		}
    }


    @Test
    public void buildInTest() {
        int[][] squares = new int[][] {
                { -2, -2, -1, -2, -1, -1, -1,  1, -2, -2},
                { -2, -2, -1, -2, -1, -1, -1, -1, -2, -2},
                { -2, -1, -1, -2, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -2, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -1, -1, -1, -2,  1, -1},
                {  0, -2, -2, -2, -2, -1, -1, -1,  1, -1},
                { -1, -2, -2, -2, -2, -2, -2, -1, -1, -1},
                { -1, -2, -1, -1, -1, -1, -2, -2, -2, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -1, -1, -1, -1, -1, -1, -1, -2, -2}};
        AIPlayerGreedy aiGreedy = new AIPlayerGreedy(3, new float[]{100, 10, 1}, 0.5f,1);
        AIBuildIn aiplayer = new AIBuildIn(3, new float[]{100, 10, 1}, 0.5f,1, aiGreedy);
        int[][] queens = AIPlayerGreedy.getQueensOfPlayer(squares, GamePlayerId.PLAYER1);
        //System.out.println(aiplayer.buildIn(squares,queens,GamePlayerId.PLAYER1));
    }
}
