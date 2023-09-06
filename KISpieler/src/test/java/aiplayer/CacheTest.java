package aiplayer;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

public class CacheTest {

    @Test
    public void b64ConversionTestRandomized(){
        Random rdm = new Random();
        int testRepetitions = 50;
        for(int j = 0; j<testRepetitions; j++){
            int randomLength = rdm.nextInt(26) * 8;
            StringBuilder randomBinStringBuilder = new StringBuilder();
            for(int i = 0; i < randomLength; i++){
                if(rdm.nextBoolean()){
                    randomBinStringBuilder.append("0");
                }
                else{
                    randomBinStringBuilder.append("1");
                }
            }
            String randomBinString = randomBinStringBuilder.toString();
            //System.out.println(randomBinString);
            String stored = Cache.binToB64(randomBinString);
            //System.out.println(stored);
            String restored = Cache.b64ToBin(stored);
            //System.out.println(restored);
            assertEquals(randomBinString, restored);
            //System.out.println("");
        }
    }

    @Test
    public void constructorTest(){
        Cache c = new Cache(10,10,4);
    }

    public int[][][] getStartBoard(){
        int[][] matrix = new int[][] {
                { -2, -1, -1, -2, -1, -1, -2, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -1, -2},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -1, -2},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -2, -1, -1, -2, -1, -1, -1},};
        int[][] myQueens = new int[][] {{0, 0}, {9, 3}, {9, 6}, {6, 9}};
        int[][] otherQueens = new int[][] {{3, 0}, {0, 3}, {0, 6}, {3, 9}};
        return new int[][][] {matrix, myQueens, otherQueens};
    }

    @Test
    public void boardBinaryConversionTest(){
        Cache c = new Cache(10,10,4);
        int[][][] startBoard = getStartBoard();
        int[][] matrix = startBoard[0];
        int[][] myQueens = startBoard[1];
        int[][] otherQueens = startBoard[2];
        String stored = c.boardToB64(matrix, myQueens, otherQueens);
        System.out.println(stored);
        int[][][] result = c.b64ToBoard(stored);
        int[][] loadedMatrix = result[0];
        int[][] loadedMyQueens = result[1];
        int[][] loadedOtherQueens = result[2];
        System.out.println(Arrays.deepToString(loadedMatrix));
        System.out.println(Arrays.deepToString(loadedMyQueens));
        System.out.println(Arrays.deepToString(loadedOtherQueens));
    }

    @Test
    public void turnBinaryConversionTest(){
        Cache c = new Cache(10,10,4);
        int[] turn = new int[] {6,0,3,3,3,6};
        String stored = c.turnToB64(turn);
        System.out.println(stored);
        int[] loadedTurn = c.b64ToTurn(stored);
        System.out.println(Arrays.toString(loadedTurn));
    }

}
