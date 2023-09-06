package aiplayer;

import model.board.Board;
import model.ids.GamePlayerId;
import model.player.Move;
import model.player.Shot;
import model.player.Turn;
import model.util.Position;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class represents an AIPlayer.
 * It utilizes a algorithm to calculate the best possible turn.
 * @author Julian
 */
public class AIPlayerAlphaBeta extends AIPlayerGreedy implements AIPlayer {

	private final int[] treeDepth;

	public AIPlayerAlphaBeta(int evaluationDepth, int[] treeDepth, float[] weights, float aggressiveness, int threads) {
		super(evaluationDepth, weights, aggressiveness, threads);
		this.treeDepth = treeDepth;
	}

	/** This method returns the highest scoring AI player turn, assuming the enemy chooses its highest scoring turn.
	 *  For more information visit: https://www.youtube.com/watch?v=l-hh51ncgDI
	 *
	 * @param board the current {@link Board} object
	 * @param id the GamePlayerId of the player the AI is playing as
	 * @return returns the best possible turn as a Turn object
	 */
	@Override
	public Turn bestTurn(Board board, GamePlayerId id, Duration maxTurnTime) {
		int[][] squares = board.toSquares();
		int[][] myQueens = getQueensOfPlayer(squares, id);
		int[][] otherQueens = getQueensOfPlayer(squares, id.other());
		ArrayList<int[]> turns = allTurns(squares, myQueens);
		ArrayList<int[]> enemyTurns = allTurns(squares, otherQueens);
		int numberOfTurns = turns.toArray().length + enemyTurns.toArray().length;
		int depth = 0;
		for (int j : treeDepth) {
			if (numberOfTurns < j) {
				depth++;
			}
		}
		System.out.println(" with depth: " + depth);
		float maxVal = Float.NEGATIVE_INFINITY;
		float alpha = Float.NEGATIVE_INFINITY;
		float beta = Float.POSITIVE_INFINITY;
		float tempResult;
		int[][] tempBoard;
		int[][] tempQueens;
		ArrayList<int[]> bestTurns = new ArrayList<>();
		long abortTime = System.currentTimeMillis() + (maxTurnTime.toMillis() - 2000);
		for (int[] turn : turns) {
			if (System.currentTimeMillis() >= abortTime) {
				System.out.println("!!! MaxTurnTime reached !!!");
				return super.bestTurn(board, id, maxTurnTime);
			}
			tempBoard = cloneAndApply(squares, turn);
			tempQueens = applyTurnToQueens(turn, myQueens);
			/*System.out.println(tempQueens[0][0]+tempQueens[0][1]+"\n"+tempQueens[1][0]+tempQueens[1][1]+"\n"+tempQueens[2][0]+tempQueens[2][1]+"\n"+tempQueens[3][0]+tempQueens[3][1]);
			String[] a = Arrays.deepToString(tempBoard).split("]");
			System.out.println(" "+a[0]+"\n"+a[1]+"\n"+a[2]+"\n"+a[3]+"\n"+a[4]+"\n"+a[5]+"\n"+a[6]+"\n"+a[7]+"\n"+a[8]+"\n"+a[9]+"\n");*/
			tempResult = bestTurnRecursive(tempBoard, tempQueens, otherQueens, depth, alpha, beta, false);
			if (tempResult > maxVal) {
				maxVal = tempResult;
				bestTurns = new ArrayList<>();
				bestTurns.add(turn);
			} else if (tempResult == maxVal) {
				bestTurns.add(turn);
			}
			alpha = Math.max(alpha, maxVal);
			if(beta <= alpha) {
				break;
			}
		}
		Random rdm = new Random();
		int rdmIndex = rdm.nextInt(bestTurns.toArray().length);
		int[] randomBestTurn = bestTurns.get(rdmIndex);
		Position from = new Position(randomBestTurn[0], randomBestTurn[1]);
		Position to = new Position(randomBestTurn[2], randomBestTurn[3]);
		Position shotAt = new Position(randomBestTurn[4], randomBestTurn[5]);
		Shot shot = new Shot(shotAt);
		Move move = new Move(from, to);
		Turn resultTurn= new Turn(move, shot, id);
		System.out.println("Calculated Turn:");
		System.out.println("Start: "+resultTurn.getMove().getStart().getX()+","+resultTurn.getMove().getStart().getY()+" End: "+resultTurn.getMove().getEnd().getX()+","+resultTurn.getMove().getEnd().getY()+" Shot: "+resultTurn.getShot().getShotPosition().getX()+","+resultTurn.getShot().getShotPosition().getY());
		return resultTurn;
	}

	/** This method calculates the best score of a board after depth many turns recursively and prunes unnecessary
	 * branches away.
	 *
	 * @param squares represents the board as an int[][] Array
	 * @param myQueens represents the AI player's Queens as an int[][] Array
	 * @param otherQueens represents the enemy's Queens as an int[][] Array
	 * @param depth is the amount of turns the AI calculates ahead
	 * @param alpha current best score the AI player's previous turn
	 * @param beta current best score the enemy's previous turn
	 * @param maxPlayer is true if its the AI player's turn. False otherwise
	 * @return the score of the best possible turn
	 */
	public float bestTurnRecursive (int[][] squares, int[][] myQueens, int[][] otherQueens, int depth, float alpha, float beta, boolean maxPlayer) {
		//Calculate score if depth is equals zero or if there are no turns to play
		if (depth == 0) {
			float myScore;
			float otherScore;
			int[][] squares2 = cloneSquares(squares);
			if (maxPlayer) {
				myScore = evaluateSquares(squares, otherQueens);
				otherScore = evaluateSquares(squares2, myQueens);
				return -(((1 - this.aggressiveness) * myScore) - (this.aggressiveness * otherScore));
			} else {
				myScore = evaluateSquares(squares, myQueens);
				otherScore = evaluateSquares(squares2, otherQueens);
				return (((1 - this.aggressiveness) * myScore) - (this.aggressiveness * otherScore));
			}
		}
		//Store the board and queens, that are needed for the next call of bestTurnRecursive
		int[][] tempBoard;
		int[][] tempQueens;
		if (maxPlayer) {
			ArrayList<int[]> turns = allTurns(squares, myQueens);
			if(turns.size()==0) {
				return Float.NEGATIVE_INFINITY;
			}
			float maxVal = Float.NEGATIVE_INFINITY;
			for (int[] turn : turns) {
				tempBoard = cloneAndApply(squares, turn);
				tempQueens = applyTurnToQueens(turn, myQueens);
				maxVal = Math.max(maxVal, bestTurnRecursive(tempBoard, tempQueens, otherQueens, depth-1, alpha, beta, false));
				alpha = Math.max(alpha, maxVal);
				//Prune if beta <= alpha
				if(beta <= alpha) {
					//System.out.println("pruned");
					break;
				}
			}
			return maxVal;
		} else {
			ArrayList<int[]> turns = allTurns(squares, otherQueens);
			if(turns.size()==0) {
				return Float.POSITIVE_INFINITY;
			}
			float minVal = Float.POSITIVE_INFINITY;
			for (int[] turn : turns) {
				tempBoard = cloneAndApply(squares, turn);
				tempQueens = applyTurnToQueens(turn, otherQueens);
				minVal = Math.min(minVal, bestTurnRecursive(tempBoard, myQueens, tempQueens, depth-1, alpha, beta, true));
				beta = Math.min(beta, minVal);
				if(beta <= alpha) {
					//System.out.println("pruned");
					break;
				}
			}
			return minVal;
		}
	}
}
