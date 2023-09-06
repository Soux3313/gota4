package aiplayer;
import model.board.Board;
import model.ids.GamePlayerId;
import model.player.Turn;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class tests all methods of the class AIPlayer.
 * @author Julian, Diana, Philip
 */
public class AIPlayerGreedyTest {

    /**
     * This method tests method reachableFields in AIPlayer class by using reachableFieldsTestHelper-method
     */
    @Test
    public void reachableFieldsTest() {
        //source is the starting field
        //ignoring is the field that has to be ignored (its value is -1,-1 if it is not used)
        //test contains all fields that reachableFields should return
        //Tests if the fields in test1 are in the arraylist that reachFields returns (just a small test)
        int[] source1 = {0, 0};
        int[] ignoring1 = {-1, -1};
        int[][] test1 = {{0, 1}, {1, 1}, {1, 0}, {2, 2}};
        this.reachableFieldsTestHelper(source1, ignoring1 ,test1);

        //Tests a larger area with more directions and other edges of the field
        int[] source2 = {7, 7};
        int[][] test2 = {{7, 6}, {6, 6}, {6, 8}, {5, 9}, {7, 8}, {7, 9}, {8, 8}, {8, 7}, {9, 7}};
        this.reachableFieldsTestHelper(source2, ignoring1, test2);

        //Tests if the ignoring-field works correctly
        int[] source3 = {7, 2};
        int[] ignoring3 = {8, 1};
        int[][] test3 = {{6, 2}, {5, 2}, {6, 3}, {7, 1}, {8, 1}, {9, 0}};
        this.reachableFieldsTestHelper(source3, ignoring3, test3);
    }

    /**
     * This method is a helper method to reachableFieldsTest
     * @param source is the position of AI PLayer
     * @param ignoring is the field that has to be ignored, if it is not used its value is {-1, -1}
     * @param test contains all the fields that reachableFields should return
     */
    private void reachableFieldsTestHelper(int[] source, int[] ignoring, int[][] test) {
        //Creates ai-player for reachableFieldsTest. The parameters are not important.
        AIPlayerGreedy ai = new AIPlayerGreedy(1, new float[1], 1, 1);
        //Creates squares as board for reachableFieldTest (-1 are empty spaces, everything else is blocked)
        final int[][] squares = new int[][] {
                {  0, -1,  2, -2, -2, -2, -1, -1, -1,  0},
                { -1, -1,  2, -2,  1, -2, -1, -1, -1, -1},
                {  2,  2, -1, -1, -2, -2, -1, -1, -1, -1},
                {  1, -1, -1, -2, -2, -2, -1, -1, -1, -1},
                { -1, -1,  4, -1,  0, -1, -1, -1, -1, -1},
                { -1,  2, -1,  1,  6,  1, -1, -1, -1, -1},
                {  0,  7, -1, -1, -1, -1, -1,  1, -1, -1},
                {  2, -1, -1,  9, -1,  1, -1, -1, -1, -1},
                { -1,  4,  8,  1,  1, -2,  1, -1, -1, -1},
                { -1, -1, -1, -2, -1,  1,  0, -1,  1, 0}};
        //Iterates through test-Array and checks each value in the ArrayList that reachableFields returns
        //Throw AssertionError if a field is missing
        int sourceX = source[0];
        int sourceY = source[1];
        int ignoringX = ignoring[0];
        int ignoringY = ignoring[1];
        ArrayList<int[]> fields = ai.reachableFields(squares, sourceX, sourceY, ignoringX, ignoringY);
        boolean itemFound;
        for (int i=0;i<test.length;i++) {
            itemFound = false;
            for(int[] field : fields) {
                if(test[i][0]==field[0] && test[i][1]==field[1]) {
                    itemFound = true;
                    break;
                }
            }
            assertTrue(itemFound);
        }
        // check if reachableFields returns the expected number of fields (not too many)
        assertEquals(test.length, fields.size());
    }

    /**
     * This method tests getQueens in AIPlayer class
     */
    @Test
    public void getQueensTest(){
        //contains the positions of all the queens from player one
        int[][] testqueens1 = {{0, 0}, {1, 0}, {5, 2}, {9, 9}};
        //fetching the id of player1
        GamePlayerId id1 = GamePlayerId.PLAYER1;
        getQueensTestHelper(testqueens1, id1);

        int[][] testqueens2 = {{0, 9}, {4, 3}, {9, 2}, {8, 0}};
        GamePlayerId id2 = GamePlayerId.PLAYER2;
        getQueensTestHelper(testqueens2, id2);
    }

    /**
     * This method is a helper method to getQueensTest
     * @param expectedQueensArray holds expected queens
     * @param id the player's id, important to know in order to retrieve the right queens
     */
    private void getQueensTestHelper(int[][] expectedQueensArray, GamePlayerId id) {
        final int[][] squares = new int[][] {
                {  0, -1, -1, -1, -1, -1, -1, -1, -1,  1},
                {  0, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1,  1, -1, -1, -1, -1, -1, -1},
                { -1, -1,  0, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {  1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1,  1, -1, -1, -1, -1, -1, -1,  0}};
        int[][] queens = AIPlayerGreedy.getQueensOfPlayer(squares,id);
        //Shows if queen was already found
        boolean queenFound;
        for(int j=0; j<expectedQueensArray.length; j++) {
            queenFound = false;
            for(int i=0; i<queens.length; i++) {
                //Queen is found if both coordinates are the same
                if (expectedQueensArray[j][0] == queens[i][0] && expectedQueensArray[j][1] == queens[i][1]) {
                    queenFound = true;
                    break;
                }
            }
            assertTrue(queenFound);
        }
        assertEquals(expectedQueensArray.length, queens.length);
    }

    /**
     * This method tests allTurns but without any possible Turns
     */
    @Test
    public void allTurnsNoTurnPossibleTest(){
        // on this board all pieces of player 1 are blocked and it is not possible to make any turn
        final int[][] squares = new int[][] {
                {  0, -2, -1, -1, -2, 0, -2, -1, -1,  1},
                {  -2, -2, -1, -1, -2, -2, -2, -1, -1, -1},
                { -1, -1, -1, -1, -2, 0, -2, -1, -1, -1},
                { -1, -1, -1, -1, -2, -2, -2, -1, -1, -1},
                { -1, 1, 1,  1, -1, -1, -1, -1, -1, -1},
                { -1, 1,  0, 1, -1, -1, -1, -1, -1, -1},
                { -1, 1, 1, 1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {  1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1,  1, -1, -1, -1, -1, -1, -1,  -1}};
        // generating playerId for player1
        GamePlayerId id = GamePlayerId.PLAYER1;
        int[][] queens = AIPlayerGreedy.getQueensOfPlayer(squares, id);
        // check if allTurns returns 0
        assertEquals(AIPlayerGreedy.allTurns(squares, queens).size(), 0);
    }

    /**
     * This method tests allTurns in AIPlayer class
     * but with just 4 possible Turns
     * compares test turns returned by allTurns with expected turns using arrayListComparison method
     */
    @Test
    public void allTurnsFourPossibleTest(){
        // create squares
        final int[][] squares = new int[][] {
                {  -1, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
                {  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1,  -1, -1, -1, -1, -1, -1, -1},
                { -1, -1,  -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -1, -2, -1, -1, -1, -1, -1},
                { -1, -2, 0, -1, -2, -1, -1, -1, -1, -1},
                {  1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -1,  1, -1, -1, -1, -1, -1, -1,  -1}};
        GamePlayerId id = GamePlayerId.PLAYER1;
        // fetch queens for player 1 (id 0)
        int[][] queens = AIPlayerGreedy.getQueensOfPlayer(squares, id);
        // fetch results from allTurns
        ArrayList<int[]> testTurns = AIPlayerGreedy.allTurns(squares, queens);
        // expectedTurns holds all turns that allTurns should return
        ArrayList<int[]> expectedTurns = new ArrayList<>();
        expectedTurns.add(new int[]{7, 2, 7, 3, 6, 3});
        expectedTurns.add(new int[]{7, 2, 6, 3, 7, 3});
        expectedTurns.add(new int[]{7, 2, 7, 3, 7, 2});
        expectedTurns.add(new int[]{7, 2, 6, 3, 7, 2});
        arrayListComparison(testTurns, expectedTurns);
        }

    /**
     * This method tests allTurns in AIPlayer class
     * but with just one possible Turn
     */
    @Test
    public void allTurnsOneMovePossibleTest(){
        // create squares
        final int[][] squares = new int[][] {
                {  0, -1, -2, -1, -1, -1, -1, -1, -1,  -1},
                {  -2, -2, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1,  -1, -1, -1, -1, -1, -1, -1},
                { -1, -1,  -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {  1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1,  1, -1, -1, -1, -1, -1, -1,  -1}};
        GamePlayerId id = GamePlayerId.PLAYER1;
        // fetch queens of player 1 (id 0)
        int[][] queens = AIPlayerGreedy.getQueensOfPlayer(squares, id);
        // fetch results from allTurns
        ArrayList<int[]> testTurn = AIPlayerGreedy.allTurns(squares, queens);
        // this should be one move, as only one move is possible
        assertTrue(java.util.Arrays.equals(testTurn.get(0), new int[]{0, 0, 0, 1, 0, 0}));
    }

    /**
     * This method tests allTurns in AIPlayer class
     * but with just 4 possible other Turns
     * compares test turns returned by allTurns with expected turns using arrayListComparison method
     */
    @Test
    public void allTurnsFewPossibleTest(){
        // create squares
        final int[][] squares = new int[][] {
                {  -1, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
                {  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -2, -2, -2, -1, -1, -1, -1},
                { -1, -1, -1, -2, -1, -2, -1, -1, -1, -1},
                { -1, -1, -1,  -2, 0, -2, -1, -1, -1, -1},
                { -1, -1,  -1, -2, -1, -2, -1, -1, -1, -1},
                { -1, -1, -1, -2, -2, -2, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {  1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1,  1, -1, -1, -1, -1, -1, -1,  -1}};
        GamePlayerId id = GamePlayerId.PLAYER1;
        // fetch queens of player 1 (id 0)
        int[][] queens = AIPlayerGreedy.getQueensOfPlayer(squares, id);
        // fetch results from allTurns
        ArrayList<int[]> testTurns = AIPlayerGreedy.allTurns(squares, queens);
        // expectedTurns holds all turns that allTurns should return
        // there are 4 possible turns, two in every possible direction from (4, 4)
        ArrayList<int[]> expectedTurns = new ArrayList<>();
        expectedTurns.add(new int[]{4, 4, 3, 4, 4, 4});
        expectedTurns.add(new int[]{4, 4, 3, 4, 5, 4});
        expectedTurns.add(new int[]{4, 4, 5, 4, 4, 4});
        expectedTurns.add(new int[]{4, 4, 5, 4, 3, 4});
        arrayListComparison(testTurns, expectedTurns);
    }

    /**
     * This method tests allTurns in AIPlayer class
     * but with just 2 queens and 8 possible Turns
     * compares test turns returned by allTurns with expected turns using arrayListComparison method
     */
    @Test
    public void allTurnsTwoQueensTest(){
        // create squares
        final int[][] squares = new int[][] {
                {  -1, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
                {  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -2, -2, -2, -2, -2, -1, -1},
                { -1, -1, -1, -2, -1, -1, -2, -2, -1, -1},
                { -1, -1, -1,  1, 0, 0, -2, -2, -1, -1},
                { -1, -1,  -1, -2, -2, -2, -2, -2, -1, -1},
                { -1, -1, -1, -2, -2, -2, 1, 1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {  1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1,  1, -1, -1, -1, -1, -1, -1,  -1}};
        GamePlayerId id = GamePlayerId.PLAYER1;
        // fetch queens of player 1 (id 0)
        int[][] queens = AIPlayerGreedy.getQueensOfPlayer(squares, id);
        // fetch results from allTurns
        ArrayList<int[]> testTurns = AIPlayerGreedy.allTurns(squares, queens);
        // expectedTurns holds all turns that allTurns should return
        ArrayList<int[]> expectedTurns = new ArrayList<>();
        // turns of queen on (4, 4)
        expectedTurns.add(new int[]{4, 4, 3, 4, 4, 4});
        expectedTurns.add(new int[]{4, 4, 3, 4, 3, 5});
        expectedTurns.add(new int[]{4, 4, 3, 5, 4, 4});
        expectedTurns.add(new int[]{4, 4, 3, 5, 3, 4});
        // turn of queen on (4, 6)
        expectedTurns.add(new int[]{4, 5, 3, 5, 3, 4});
        expectedTurns.add(new int[]{4, 5, 3, 5, 4, 5});
        expectedTurns.add(new int[]{4, 5, 3, 4, 3, 5});
        expectedTurns.add(new int[]{4, 5, 3, 4, 4, 5});
        arrayListComparison(testTurns, expectedTurns);
    }


    /**
     * This method is a helper method which compares two arrayLists on having the same values, order does not matter
     * This method FAILS when two lists do not have the same values
     * @param thisList is the first list to be compared to otherList
     * @param otherList is the other list to be compared to thisList
     */
    public void arrayListComparison(ArrayList<int[]> thisList, ArrayList<int[]> otherList){
        // check size, so that no list has more elements than the other
        assertEquals(thisList.size(), otherList.size());
        for (int i=0; i < thisList.size(); i++){
            // this should turn true by the end of the second for loop if i'th element of thisList is in the otherList
            boolean containsElement = false;
            for (int j=0; j < otherList.size(); j++){
                // compare elements
                if (java.util.Arrays.equals(otherList.get(j), thisList.get(i))) {
                    containsElement = true;
                    break;
                }
            } assertTrue(containsElement);
        }
    }

    /**
     * Tests if the returned value of the board is the same as expected.
     */
    @Test
    public void evaluateSquaresTest() {
        //weights of the fields. In this case the evaluationDepth is 1, so only 1 weight is required
        float [] weights1 = {1};
        AIPlayerGreedy ai = new AIPlayerGreedy(1, weights1, 1, 1);
        //squares has the same format as the int[][], that cloneAndApply returns, which means everything that
        // an empty field is -1 and the rest is -2 (pieces too)
        int[][] squares = new int[][] {
                { -2, -1, -1, -2, -1, -1, -1, -1, -2, -2},
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -1},
                { -2, -2, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -2, -2, -2, -1, -1, -1, -1, -2},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -1, -2, -2, -1, -2, -1, -2},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -2},
                { -2, -2, -1, -1, -1, -2, -1, -2, -1, -2},
                { -2, -2, -1, -1, -1, -1, -1, -1, -1, -1},
                { -2, -2, -2, -1, -1, -2, -1, -2, -2, -2}};
        //Positions of 4 queens
        int [][] queens = new int[][] {{0,0},{1,0},{7,7},{0,9}};
        float result = ai.evaluateSquares(squares,queens);
        //the queens can reach 12 fields in one turn
        //delta is the deviation that the result is allowed to have, but we want it to be exactly 12 so our delta is 0.
        assertEquals(12, result, 0);

        //the first/second/third entry of the array is the weight for fields that can be reached first/second/third
        float [] weights2 = {3, 2, 1};
        ai = new AIPlayerGreedy(3, weights2, 1, 1);
        squares = new int[][] {
                { -2, -1, -2, -2, -2, -2, -2, -1, -2, -2},
                { -2, -2, -2, -2, -1, -1, -2, -1, -2, -2},
                { -2, -2, -2, -2, -2, -1, -2, -1, -1, -1},
                { -1, -1, -2, -1, -1, -1, -2, -1, -1, -1},
                { -1, -2, -2, -1, -2, -2, -2, -1, -1, -1},
                { -1, -2, -2, -1, -2, -2, -1, -2, -1, -2},
                { -1, -2, -2, -2, -2, -2, -1, -1, -1, -2},
                { -2, -2, -1, -1, -1, -2, -1, -2, -1, -2},
                { -2, -2, -1, -1, -1, -1, -1, -1, -1, -1},
                { -2, -2, -1, -1, -1, -2, -1, -2, -2, -2}};
        queens = new int[][] {{1,3},{5,4},{9,0},{0,9}};
        result = ai.evaluateSquares(squares,queens);
        //4 fields give 3 points because they can be reached in 1 turn. 4 fields give 2 points. No field will be reached in 3 turns
        assertEquals((3*4)+(2*4), result, 0);

        float [] weights3 = {3, 2, 1};
        ai = new AIPlayerGreedy(3, weights3, 1, 1);
        squares = new int[][] {
                { -2, -1, -2, -2, -2, -2, -2, -1, -2, -2},
                { -2, -2, -2, -2, -1, -1, -2, -1, -2, -2},
                { -2, -2, -2, -2, -2, -1, -2, -1, -1, -1},
                { -2, -1, -2, -1, -2, -1, -2, -1, -1, -1},
                { -1, -2, -2, -1, -2, -2, -2, -1, -1, -1},
                { -2, -2, -2, -1, -1, -1, -2, -2, -1, -2},
                { -1, -2, -2, -2, -2, -2, -1, -1, -1, -2},
                { -2, -2, -1, -1, -1, -2, -1, -2, -2, -2},
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -1},
                { -2, -2, -1, -1, -1, -2, -1, -2, -2, -2}};
        queens = new int[][] {{3,4},{5,0},{3,0},{9,9}};
        result = ai.evaluateSquares(squares,queens);
        assertEquals(3*8+2*4+1*1, result, 0);
    }

    /**
     * Tests if the ai returns the best possible turn on some simple boards
     */
    @Test
    public void bestTurnTest() {
        float [] weights = {25,16,9,4,1};
        AIPlayer ai = new AIPlayerGreedy(5, weights, 0.5f, 1);
        Integer[][] squares = new Integer[][] {
                {  0, -2, -1, -1, -1, -1, -1, -1, -2,  1}, //0
                {  0, -2, -1, -1, -1, -1, -1, -1, -2, -2}, //1
                { -2, -2, -1, -2, -1, -1, -1, -1, -1, -1}, //2
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, //3
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1}, //4
                { -1, -2,  0, -1, -2, -1, -1, -1, -1, -1}, //5
                { -1, -2, -2, -1, -2, -1, -1, -1, -1, -1}, //6
                { -2, -2, -1, -2, -1, -1, -1, -1, -1, -1}, //7
                {  1, -1, -1, -1, -1, -1, -1, -1, -2, -2}, //8
                {  1, -2, -1, -1, -1, -1, -1, -1, -2,  0}};//9
        //         0   1   2   3   4   5   6   7   8   9
        Board board = Board.fromSquares(10, 10, squares);
        Turn result = ai.bestTurn(board, GamePlayerId.PLAYER1, Duration.ofSeconds(60));
        //Prints the best possible move :)
        //System.out.println("Start: "+result.getMove().getStart().getX()+","+result.getMove().getStart().getY()+" End: "+result.getMove().getEnd().getX()+","+result.getMove().getEnd().getY()+" Shot: "+result.getShot().getShotPosition().getX()+","+result.getShot().getShotPosition().getY());
        //Test if the right queen was chosen
        assertEquals(5,result.getMove().getStart().getX());
        assertEquals(2,result.getMove().getStart().getY());
        //Tests if the queen was moved to the right field
        assertEquals(8,result.getMove().getEnd().getX());
        assertEquals(5,result.getMove().getEnd().getY());
        //Tests if the ki shot on the right field
        assertEquals(8,result.getShot().getShotPosition().getX());
        assertEquals(1,result.getShot().getShotPosition().getY());

        squares = new Integer[][] {
                { -1, -1, -1, -1, -1, -2,  1, -2, -1, -1}, //0
                {  1, -1, -1, -1, -1, -2, -2, -2, -1, -1}, //1
                { -2, -2, -2, -1, -2, -2, -1, -1, -2, -2}, //2
                {  1, -2, -2, -1, -2, -1, -1, -1, -2,  1}, //3
                { -2, -2, -1, -1, -2, -1, -1, -1, -2, -2}, //4
                { -2, -2, -1, -1, -2, -1, -1, -1, -2, -2}, //5
                {  0, -2, -1, -1, -2, -1, -1, -1, -2,  0}, //6
                { -2, -2, -2, -1, -2, -1, -1, -1, -1, -1}, //7
                { -1, -1, -2, -1, -1, -2, -2, -2, -1, -1}, //8
                { -1, -1, -2,  0, -1, -2,  0, -2, -1, -1}};//9
        //         0   1   2   3   4   5   6   7   8   9
        board = Board.fromSquares(10, 10, squares);
        result = ai.bestTurn(board, GamePlayerId.PLAYER1, Duration.ofSeconds(60));
        //Test if the right queen was chosen
        assertEquals(9,result.getMove().getStart().getX());
        assertEquals(3,result.getMove().getStart().getY());
        //Tests if the queen was moved to the right field
        assertEquals(0,result.getMove().getEnd().getX());
        assertEquals(3,result.getMove().getEnd().getY());
        //Tests if the ki shot on the right field
        assertEquals(1,result.getShot().getShotPosition().getX());
        assertEquals(2,result.getShot().getShotPosition().getY());

        squares = new Integer[][] {
                { -2, -2, -1, -1, -1, -2,  1, -2, -1, -1}, //0
                {  1, -2, -1, -1, -1, -2, -1, -2, -1, -1}, //1
                { -2, -2, -2, -2, -2, -2, -1, -1, -2, -2}, //2
                {  1, -2, -2, -1, -2, -1, -1, -1, -2,  1}, //3
                { -2, -2, -1, -1, -2, -1, -1, -1, -2, -2}, //4
                { -2, -1,  0, -1, -1, -1, -1, -1, -2, -2}, //5
                {  0, -2, -1, -1, -2, -1, -1, -1, -2,  0}, //6
                { -2, -2, -2, -1, -2, -1, -1, -1, -2, -2}, //7
                { -1, -1, -2, -1, -2, -2, -2, -2, -1, -1}, //8
                { -1, -1, -2, -2, -1, -2,  0, -2, -1, -1}};//9
        //         0   1   2   3   4   5   6   7   8   9
        board = Board.fromSquares(10, 10, squares);
        result = ai.bestTurn(board, GamePlayerId.PLAYER1, Duration.ofSeconds(60));
        //Test if the right queen was chosen
        assertEquals(5,result.getMove().getStart().getX());
        assertEquals(2,result.getMove().getStart().getY());
        //Tests if the queen was moved to the right field
        assertEquals(5,result.getMove().getEnd().getX());
        assertEquals(6,result.getMove().getEnd().getY());

        squares = new Integer[][] {
                { -1, -1, -1, -2, -2, -2,  1, -2, -1, -1}, //0
                {  1, -1, -2,  1, -2, -2, -2, -2, -1, -1}, //1
                { -2, -2, -1, -1, -2, -2, -1, -1, -2, -2}, //2
                {  1, -2, -1, -1, -2, -1, -1, -1, -2,  1}, //3
                { -2, -2, -1, -1, -2, -1, -1, -1, -2, -2}, //4
                { -2, -2, -1, -1, -2, -1, -1, -1, -2, -2}, //5
                {  0, -2, -1, -1, -2, -1, -1, -1, -2,  0}, //6
                { -2, -2, -2, -1, -2, -1, -1, -1, -1, -1}, //7
                { -1, -1, -2, -1, -1, -1, -1, -2, -1, -1}, //8
                { -1, -1, -1,  0, -1, -1,  0, -1, -1, -1}};//9
        //         0   1   2   3   4   5   6   7   8   9
        //We create an 100% agressive ai to see if he is able to lock the piece on (1,3) in
        AIPlayer ai2 = new AIPlayerGreedy(5, weights, 1, 1);
        board = Board.fromSquares(10, 10, squares);
        result = ai2.bestTurn(board, GamePlayerId.PLAYER1, Duration.ofSeconds(60));
        //Test if the right queen was chosen
        assertEquals(9,result.getMove().getStart().getX());
        assertEquals(3,result.getMove().getStart().getY());
        //Tests if the queen was moved to the right field
        assertEquals(2,result.getMove().getEnd().getX());
        assertEquals(3,result.getMove().getEnd().getY());
        //Tests if the ki shot on the right field
        assertEquals(2,result.getShot().getShotPosition().getX());
        assertEquals(2,result.getShot().getShotPosition().getY());
    }


    @Test
    public void cloneAndApplyTest() {
        //Starting squares
        int[][] squares = new int[][] {
                {  0, -2, -1, -1, -1, -1, -1, -1, -2,  1},
                {  0, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -1, -1, -2, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2,  0, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {  1, -1, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -1,  1, -1, -1, -1, -1, -1, -2,  0}};

        //Diagonal shots
        //Queen moves horizontally and shoots diagonally
        int[][] squaresResult = new int[][] {
                { -1, -2,  -2, -1, -1, -1, -1, -1, -2, -2},
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -1, -1, -2, -2, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -1,  -2, -1, -1, -1, -1, -1, -2, -2}};

        //Queen moves vertically and shoots diagonally
        int[][] squaresResult2 = new int[][] {
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -1, -1, -2, -1, -1, -1, -1, -1, -1},
                { -1, -1, -2, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -1, -1, -1, -1, -1, -1, -1},
                { -2, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -1, -2, -1, -1, -1, -1, -1, -2, -2}};

        //Queen moves diagonally and shoots diagonally
        int[][] squaresResult3 = new int[][] {
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -1},
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -1, -1, -2, -1, -1, -1, -2, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -1, -1, -2, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -1, -2, -1, -1, -1, -1, -1, -2, -2}};

        //Horizontal shots
        //Queen moves horizontally and shoots horizontally
        int[][] squaresResult4 = new int[][] {
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -1, -1, -2, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -1, -2, -2, -2, -1, -2, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -1, -2, -1, -1, -1, -1, -1, -2, -2}};

        //Queen moves vertically and shoots horizontally
        int[][] squaresResult5 = new int[][] {
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -1, -1, -2, -1, -1, -1, -1, -1, -1},
                { -1, -1, -2, -1, -1, -1, -2, -1, -1, -1},
                { -1, -2, -2, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -1, -1, -1, -1, -1, -1, -1, -2, -2}};

        //Queen moves diagonally and shoots horizontally
        int[][] squaresResult6 = new int[][] {
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -2, -2, -2, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -1, -2, -1, -1, -1, -1, -1, -2, -1}};

        //Vertical shots
        //Queen moves horizontally and shoots vertically
        int[][] squaresResult7 = new int[][] {
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -1, -1, -2, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -2, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -2, -2, -2},
                { -1, -1, -2, -1, -1, -1, -1, -1, -2, -2}};

        //Queen moves vertically and shoots vertically
        int[][] squaresResult8 = new int[][] {
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -1, -1, -2, -1, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -1, -2, -1, -1, -1, -1, -1, -2, -2}};

        //Queen moves diagonally and shoots vertically
        int[][] squaresResult9 = new int[][] {
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -2, -1, -1, -1, -1, -1, -1, -2, -2},
                { -2, -1, -1, -2, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -2, -2, -1, -1, -1, -1, -2, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -2, -1, -1},
                { -2, -1, -1, -1, -1, -1, -1, -1, -2, -2},
                { -1, -1, -1, -1, -1, -1, -1, -1, -2, -2}};


        int[] turn = {0, 0, 0, 2, 2, 4};
        int[] turn2 = {1, 0, 5, 0, 3, 2};
        int[] turn3 = {0, 9, 4, 5, 2, 7};
        int[] turn4 = {5, 2, 5, 5, 5,7};
        int[] turn5 = {9, 2, 3, 2, 3, 6};
        int[] turn6 = {9, 9, 2, 2, 2, 1};
        int[] turn7 = {8, 0, 8, 7, 6, 7};
        int[] turn8 = {1, 0, 7, 0, 3, 0};
        int[] turn9 = {9, 2, 4, 7, 7, 7};

        int[][] result = AIPlayerGreedy.cloneAndApply(squares, turn);
        int[][] result2 = AIPlayerGreedy.cloneAndApply(squares, turn2);
        int[][] result3 = AIPlayerGreedy.cloneAndApply(squares, turn3);
        int[][] result4 = AIPlayerGreedy.cloneAndApply(squares, turn4);
        int[][] result5 = AIPlayerGreedy.cloneAndApply(squares, turn5);
        int[][] result6 = AIPlayerGreedy.cloneAndApply(squares, turn6);
        int[][] result7 = AIPlayerGreedy.cloneAndApply(squares, turn7);
        int[][] result8 = AIPlayerGreedy.cloneAndApply(squares, turn8);
        int[][] result9 = AIPlayerGreedy.cloneAndApply(squares, turn9);

        //Queen moves in all directions and shoots diagonally
        assertTrue(Arrays.deepEquals(result, squaresResult));
        assertTrue(Arrays.deepEquals(result2, squaresResult2));
        assertTrue(Arrays.deepEquals(result3, squaresResult3));

        //Queen moves in all directions and shoots horizontally
        assertTrue(Arrays.deepEquals(result4, squaresResult4));
        assertTrue(Arrays.deepEquals(result5, squaresResult5));
        assertTrue(Arrays.deepEquals(result6, squaresResult6));

        //Queen moves in all directions and shoots vertically
        assertTrue(Arrays.deepEquals(result7, squaresResult7));
        assertTrue(Arrays.deepEquals(result8, squaresResult8));
        assertTrue(Arrays.deepEquals(result9, squaresResult9));
    }

    /**
     * Tests if the expectedQueens are the same as the given queens with the turn applied to them
     */
    @Test
    public void applyTurnToQueensTest() {
        int[] turn = {0, 0, 0, 9, 0, 3};
        int[][] queensBeforeTurn = {{1,1}, {3,3}, {2,2}, {0,0}};
        //These are the queens that are expected after the turn was applied to queensBeforeTurn
        int[][] expectedQueens = {{0,9}, {1,1}, {2,2}, {3,3}};
        //Search expected queens in the queens that applyTurnToQueens returns
        findExpectedQueensInResultQueens(expectedQueens, turn, queensBeforeTurn);

        turn = new int[] {3, 3, 6, 0, 3, 3};
        queensBeforeTurn = new int[][] {{2,2}, {1,1}, {3,3}, {0,0}};
        expectedQueens = new int[][] {{0,0}, {1,1}, {2,2}, {6,0}};
        findExpectedQueensInResultQueens(expectedQueens, turn, queensBeforeTurn);
    }

    /**
     * This method helps to compare the expectedQueens in the resultQueens. If one queen of the expectedQueens is not found in
     * the resultQueens a AssertionError is thrown.
     * @param expectedQueens are the queens that are expected to get returned by applyTurnToQueens
     * @param turn is the turn that is applied to queens
     * @param queens are the queens that will be used to apply the turn
     */
    private void findExpectedQueensInResultQueens(int[][] expectedQueens, int[] turn, int[][] queens) {
        float [] weights = {25,16,9,4,1};
        AIPlayerGreedy ai = new AIPlayerGreedy(5, weights, 0.5f, 1);
        int[][] resultQueens = AIPlayerGreedy.applyTurnToQueens(turn,queens);
        boolean queenFound;
        assertEquals(resultQueens.length,expectedQueens.length);
		for (int[] expectedQueen : expectedQueens) {
			queenFound = false;
			for (int j = 0; j < resultQueens.length; j++) {
				if (expectedQueen[0] == resultQueens[j][0] && expectedQueen[1] == resultQueens[j][1]) {
					queenFound = true;
					break;
				}
			}
			assertTrue(queenFound);
		}
    }

    /**
     * this method tests a few turns with evaluateTurn in AIPlayer class
     * Expected values are calculated by hand
     * test cases are very much simplified
     */
    @Test
    public void evaluateSimpleTurnTest(){
        // create an aiplayer with depth 3
        AIPlayerGreedy aiplayer = new AIPlayerGreedy(3, new float[]{100, 10, 1}, 0.5f, 1);
        // create squares
        final int[][] squares = new int[][] {
                {  -2, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                {  -2, 0, -2, -1, -2, -1, -1, -1, -1, -1},
                { -2, -1, -2, -1, -2, -1, -1, -1, -1, -1},
                { -2, -1, -2, 1, -2, -1, -1, -1, -1, -1},
                { -2, -1, -2, -1, -2, -1, -1, -1, -1, -1},
                { -2, -1, -1, -1, -2, -1, -1, -1, -1, -1},
                { -2, -2, -2, -2, -2, -1, -1, -1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1,  -1, -1, -1, -1, -1, -1, -1, -1}};
        // getting the queens is necessary for evaluateTurn method
        // create id to retrieve own queens
        GamePlayerId id1 = GamePlayerId.PLAYER1;
        int[][] myQueens = AIPlayerGreedy.getQueensOfPlayer(squares, id1);
        // create id to retrieve other queens
        GamePlayerId id2 = GamePlayerId.PLAYER2;
        int[][] otherQueens = AIPlayerGreedy.getQueensOfPlayer(squares, id2);

        // make a bad move where aiplayer just moves the queen one step further and shoots the arrow on its source
        int[] testTurn1 = new int[]{1, 1, 2, 1, 1, 1};
        float testScore1 = aiplayer.evaluateTurn(squares, testTurn1, myQueens, otherQueens);
        // score is: (0.5*321)-(0.5*422) = -50.5
        assertEquals(-50.5f, testScore1, 0);

        // make a good move where aiplayer blocks enemies' way
        int[] testTurn2 = new int[]{1, 1, 5, 1, 5, 3};
        float testScore2 = aiplayer.evaluateTurn(squares, testTurn2, myQueens, otherQueens);
        // score is: (0.5*510)-(0.5*311) = 99.5
        assertEquals(99.5f, testScore2, 0);

    }

    /**
     * this method tests a few turns with evaluateTurn in AIPlayer class
     * evaluateSquares() method in AIPlayer class is required to work in this method
     * test cases are more complex
     */
    @Test
    public void evaluateComplexTurn(){
        final int[][] squares = new int[][] {
                {  -1, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
                {  -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1, -1, -2, -2, -2, -2, -2, -1, -1},
                { -1, -1, -1, -2, -1, -1, -2, -2, -1, -1},
                { -1, -1, -1,  1, 0, 0, -2, -2, -1, -1},
                { -1, -1, -1, -2, -2, -2, -2, -2, -1, -1},
                { -1, -1, -1, -2, -2, -2, 1, 1, -1, -1},
                { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {  1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                { -1, -1,  1, -1, -1, -1, -1, -1, -1, -1}};
    }
}