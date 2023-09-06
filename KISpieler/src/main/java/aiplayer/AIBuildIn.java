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
 * Every Turn the AI checks if its in the build-in phase. If so it uses a different algorithm to build itself in as efficient as possible, if not
 * return the best turn of the given ai.
 * @author Julian
 */
public class AIBuildIn extends AIPlayerGreedy {
	private final AIPlayer ai;

	public AIBuildIn(int evaluationDepth, float[] weights, float aggressiveness, int threads, AIPlayer ai) {
		super(evaluationDepth, weights, aggressiveness, threads);
		this.ai = ai;
	}

	/**
	 * This method checks if all queens are build in and how much space they have left.
	 * @param board the current {@link Board} object
	 * @param id    the GamePlayerId of the player the AI is playing as
	 * @return returns the best possible turn as a Turn object
	 */
	@Override
	public Turn bestTurn(Board board, GamePlayerId id, Duration maxTurnTime) {
		System.out.println("-----------------------Calculating...----------------------");
		int[][] squares = board.toSquares();
		int[][] myQueens = getQueensOfPlayer(squares, id);
		int freeFields = buildIn(squares, getFreeQueens(squares, myQueens), id);
		if(freeFields == -1) {
			System.out.print("Using: Alpha-Beta-Pruning");
			return ai.bestTurn(board, id, maxTurnTime);
		}
		if(freeFields <= 9) {
			System.out.println("Using: Build-In Deep");
			System.out.println("Free fields: " + freeFields);
			return bestBuildInTurn(squares, myQueens, freeFields, id);
		} else {
			System.out.println("Using: Build-In Shallow");
			return super.bestTurn(board, id, maxTurnTime);
		}
	}

	/** This method calculates, if the AI is build.in and if so how many free fields it has left.
	 *
	 * @param squares the squares array from {@link Board}, empty fields are -1, blocked fields are -2, queens are 0 or 1.
	 * @param queens the position of the queens of the AI
	 * @param id the GamePlayerId of the player the AI is playing as
	 * @return returns -1 if the AI is not build-in. Returns the number of free fields left, if the AI is build-in.
	 */
	public int buildIn(int[][] squares, int[][] queens, GamePlayerId id) {
		int[][] squares2 = cloneSquares(squares);
		ArrayList<int[]> fields = new ArrayList<>();
		int freeFields = 0;
		for (int[] queen : queens) {
			fields.add(new int[]{queen[0], queen[1]});
		}
		ArrayList<int[]> neighbours;
		while (!fields.isEmpty()) {
			neighbours = getNeighbours(fields.get(0));
			for (int[] neighbour : neighbours) {
				if (squares2[neighbour[0]][neighbour[1]] == id.other().get()) {
					return -1;
				}
				if (squares2[neighbour[0]][neighbour[1]] == -1) {
					freeFields++;
					fields.add(new int[]{neighbour[0], neighbour[1]});
					squares2[neighbour[0]][neighbour[1]] = -2;
				}
			}
			fields.remove(0);
		}
		return freeFields;
	}

	/**
	 * This method returns all possible neighbours in an ArrayList object
	 * @param position then position on squares as an array with length 2
	 * @return return all possible neighbours in an ArrayList
	 */
	public ArrayList<int[]> getNeighbours(int[] position) {
		ArrayList<int[]> result = new ArrayList<>();
		int[] tempPos;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0) {
					continue;
				}
				tempPos = new int[2];
				tempPos[0] = position[0] + i;
				tempPos[1] = position[1] + j;
				if (!(tempPos[0] < 0 || tempPos[1] < 0 || tempPos[0] > 9 || tempPos[1] > 9)) {
					result.add(tempPos);
				}
			}
		}
		return result;
	}

	/**
	 * This method returns all queens from the given queens that still have free neighbouring fields
	 * @param squares the board as an array
	 * @param queens the coordinates of the given queens
	 * @return returns the queens in the same format in which they were given
	 */
	public int[][] getFreeQueens(int[][] squares,int[][] queens) {
		ArrayList<int[]> queensList = new ArrayList<>();
		ArrayList<int[]> neighbours;
		for (int[] queen : queens) {
			neighbours = getNeighbours(queen);
			for (int[] neighbour : neighbours) {
				if (squares[neighbour[0]][neighbour[1]] == -1) {
					queensList.add(new int[]{queen[0], queen[1]});
					break;
				}
			}
		}
		// convert to array as all methods expect an array
		int queen_index = 0;
		int[][] newQueens = new int[queensList.size()][2];
		for (int[] queen : queensList) {
			newQueens[queen_index] = queen;
			queen_index++;
		}
		return newQueens;
	}

	/**
	 * This method tries to find the best way to build itself in by trying to apply as many turns as possible before the board is too full. If we applied as
	 * many turns as we had free fields in the beginning (gets calculated in best turn) we can break the loop and return the result because every free field we
	 * had will be used.
	 * @param squares represents the board as an int[][] Array
	 * @param queens represents the AI player's Queens as an int[][] Array
	 * @param freeFields the amount of free fields
	 * @param id the GamePlayerId of the player the AI is playing as
	 * @return returns the best possible turn
	 */
	public Turn bestBuildInTurn(int[][] squares, int[][] queens,int freeFields, GamePlayerId id) {
		ArrayList<int[]> turns = allTurns(squares, queens);
		int maxVal = Integer.MIN_VALUE;
		int tempResult;
		int[][] tempBoard;
		int[][] tempQueens;
		ArrayList<int[]> bestTurns = new ArrayList<>();
		for (int[] turn : turns) {
			tempBoard = cloneAndApply(squares, turn);
			tempQueens = applyTurnToQueens(turn, queens);
			tempResult = bestBuildInTurnRecursive(tempBoard, tempQueens,1, freeFields);
			if (tempResult > maxVal) {
				maxVal = tempResult;
				bestTurns = new ArrayList<>();
				bestTurns.add(turn);
			} else if (tempResult == maxVal) {
				bestTurns.add(turn);
			}
		}
		System.out.println("Path found using "+ maxVal +" fields");
		Random rdm = new Random();
		int rdmIndex = rdm.nextInt(bestTurns.toArray().length);
		int[] randomBestTurn = bestTurns.get(rdmIndex);
		Position from = new Position(randomBestTurn[0], randomBestTurn[1]);
		Position to = new Position(randomBestTurn[2], randomBestTurn[3]);
		Position shotAt = new Position(randomBestTurn[4], randomBestTurn[5]);
		Shot shot = new Shot(shotAt);
		Move move = new Move(from, to);
		Turn resultTurn = new Turn(move, shot, id);
		System.out.println("Start: "+resultTurn.getMove().getStart().getX()+","+resultTurn.getMove().getStart().getY()+" End: "+resultTurn.getMove().getEnd().getX()+","+resultTurn.getMove().getEnd().getY()+" Shot: "+resultTurn.getShot().getShotPosition().getX()+","+resultTurn.getShot().getShotPosition().getY());
		return resultTurn;
	}

	/**
	 * This method calculates the resulting max-depths of each turn that is applied recursively.
	 * @param squares represents the board as an int[][] Array
	 * @param queens represents the AI player's Queens as an int[][] Array
	 * @param curDepth the current depth (in other words the amount of turns that have been applied on the same board)
	 * @param freeFields the amount of free fields
	 * @return returns the maximum depth
	 */
	public int bestBuildInTurnRecursive(int[][] squares, int[][] queens, int curDepth, int freeFields) {
		ArrayList<int[]> turns = allTurns(squares, queens);
		if (turns.size() == 0) {
			return curDepth;
		}
		int[][] tempBoard;
		int[][] tempQueens;
		int maxVal = Integer.MIN_VALUE;
		for (int[] turn : turns) {
			tempBoard = cloneAndApply(squares, turn);
			tempQueens = applyTurnToQueens(turn, queens);
			maxVal = Math.max(maxVal, bestBuildInTurnRecursive(tempBoard, tempQueens, curDepth+1, freeFields));
			if (maxVal == freeFields) {
				return maxVal;
			}
		}
		return maxVal;
	}
}
