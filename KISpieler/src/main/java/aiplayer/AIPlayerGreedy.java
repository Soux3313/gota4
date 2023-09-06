package aiplayer;

import model.board.Board;
import model.ids.GamePlayerId;
import model.player.Move;
import model.player.Shot;
import model.player.Turn;
import model.util.Position;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * This class represents an AIPlayer.
 * It utilizes a greedy algorithm to calculate the best possible turn according to its evaluation.
 * @author Julian, Yannick
 */
public class AIPlayerGreedy implements AIPlayer {


	/**
	 * The evaluationDepth defines the max amount of turns a player can use to reach a field for it to be scored.
	 * A value of 5 seems fine.
	 */
	protected final int evaluationDepth;

	/**
	 * The weights array defines how many points the AI gets for being able to reach a field in x turns.
	 * Being able to reach a field in x turns grants weights[x] points.
	 * The values should be in descending order.
	 * The length of weights needs to be equal to evaluationDepth.
	 */
	protected final float[] weights;

	/**
	 * The aggressiveness defines how the AI values the enemies score compared to its own score.
	 * A value of 0.5 is neutral, it values both scores equally.
	 * A value >0.5 is aggressive, it values the enemies score more, and will therefore try to reduce it more.
	 * A value <0.5 is defensive, it values its own score more, and will therefore try to maximise it more.
	 * Values reach between 0 and 1.
	 */
	protected final float aggressiveness;

	/**
	 * How many threads (cpu cores) the Ai uses while evaluating all plays.
	 * If threads is 0, it uses as may threads as the processor has cores.
	 * If threads is smaller 2, all the plays will be evaluated sequentially.
	 * If threads is bigger 1, 'thread' plays will be evaluated concurrently, utilizing multiple cpu cores.
	 * For a small explanation how we use thread pools visit : https://www.geeksforgeeks.org/thread-pools-java/
	 */
	protected final int threads;

	/**
	 * An array of all 8 directions a queen can travel in, represented as vectors.
	 * Useful in a few functions, therefore defined for the whole class.
	 */
	protected static final int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {-1, -1}, {1, -1}};

	public AIPlayerGreedy(int evaluationDepth, float[] weights, float aggressiveness, int threads) {
		assert weights.length == evaluationDepth;
		assert aggressiveness >= 0 && aggressiveness <= 1;
		this.aggressiveness = aggressiveness;
		this.weights = weights;
		this.evaluationDepth = evaluationDepth;
		if (threads == 0) {
			this.threads = Runtime.getRuntime().availableProcessors();
		} else {
			this.threads = threads;
		}
	}

	/**
	 * This method returns all the coordinates of fields that you can reach starting from the source.
	 * The method treats the "ignoring"-field like an empty field,
	 * when no field should be ignored, ignored can be x=-1, y=-1.
	 *
	 * @param squares   the squares array from {@link Board}, empty fields are -1
	 * @param sourceX   the x coordinate of the source
	 * @param sourceY   the y coordinate of the source
	 * @param ignoringX the x coordinate of the ignored field
	 * @param ignoringY the y coordinate of the ignored field
	 * @return an ArrayList of int[2] of the form {x,y}, where the x,y are the coordinates of all reachable fields
	 */
	public static ArrayList<int[]> reachableFields(int[][] squares, int sourceX, int sourceY, int ignoringX, int ignoringY) {
		ArrayList<int[]> possibleLocations = new ArrayList<>();
		int curX;
		int curY;
		//Iterates possible directions in which the queen could move
		for (int[] direction : directions) {
			//1 step in the direction is added
			curX = sourceX + direction[0];
			curY = sourceY + direction[1];
			//While I am still on the board and am not standing on another piece, that is not being ignored
			while (curX < squares.length && curY < squares[0].length && 0 <= curX && 0 <= curY &&
					(squares[curX][curY] == -1 || (curX == ignoringX && curY == ignoringY))) {
				//add the position to the result
				possibleLocations.add(new int[]{curX, curY});
				//1 step in the direction is added
				curX += direction[0];
				curY += direction[1];
			}
		}
		return possibleLocations;
	}

	/**
	 * This method returns all possible valid turns a player an take give the current field.
	 *
	 * @param squares the squares array from {@link Board}, empty fields are -1
	 * @param queens  an Array of length nr of queens containing the coordinates {x, y} of the players queens,
	 *                obtained from {@link #getQueensOfPlayer}
	 * @return an ArrayList of int[6] of the form {fromX, fromY, toX, toY, shotX, shotY}.
	 * This contains all the information to make a Turn object.
	 * We chose not to create a Turn object here for runtimes sake.
	 */
	public static ArrayList<int[]> allTurns(int[][] squares, int[][] queens) {
		// declare all the vars used in the loop
		int fromX;
		int fromY;
		int toX;
		int toY;
		int shotX;
		int shotY;
		ArrayList<int[]> movements;
		ArrayList<int[]> shots;
		ArrayList<int[]> result = new ArrayList<>();
		for (int[] queen : queens) {
			fromX = queen[0];
			fromY = queen[1];
			// calculate all movements from the position of a queen
			movements = reachableFields(squares, fromX, fromY, -1, -1);
			for (int[] movement : movements) {
				toX = movement[0];
				toY = movement[1];
				// calculate all shot from a possible position of a queen
				// ignoring its original position, as it would be empty
				shots = reachableFields(squares, toX, toY, fromX, fromY);
				for (int[] shot : shots) {
					shotX = shot[0];
					shotY = shot[1];
					result.add(new int[]{fromX, fromY, toX, toY, shotX, shotY});
				}
			}
		}
		return result;
	}

	/**
	 * This method clones the board and applies the given turn on it.
	 *
	 * @param squares the squares array from {@link Board}, empty fields are -1.
	 * @param turn    is an int[6] of the form {fromX, fromY, toX, toY, shotX, shotY}.
	 * @return an int[][] as the new board. -1 are empty fields and -2 are non-empty fields.
	 */
	public static int[][] cloneAndApply(int[][] squares, int[] turn) {
		int[][] result = new int[squares.length][squares[0].length];
		//Copy squares to result, but replay every piececode with -2, because we will write other values on the board later on
		for (int i = 0; i < squares.length; i++) {
			for (int j = 0; j < squares[i].length; j++) {
				if (squares[i][j] == -1) {
					result[i][j] = -1;
				} else {
					result[i][j] = -2;
				}
			}
		}
		//Replace the field where the queen was with an empty field
		result[turn[0]][turn[1]] = -1;
		//Replace the field where the queen goes to with an non-empty field
		result[turn[2]][turn[3]] = -2;
		//Replace the field where the arrow goes to with an non-empty field
		result[turn[4]][turn[5]] = -2;
		return result;
	}

	/**
	 * Applies a turn to the queens array, so it changes the position of the moved queen to its new position.
	 *
	 * @param turn   is an int[6] of the form {fromX, fromY, toX, toY, shotX, shotY}.
	 * @param queens an Array of length nr of queens containing the coordinates {x, y} of the players queens,
	 *               obtained from {@link #getQueensOfPlayer}
	 * @return a queens array with the turn applied to it.
	 */
	public static int[][] applyTurnToQueens(int[] turn, int[][] queens) {
		int[][] newQueens = new int[queens.length][2];
		int[] fromPosition = new int[]{turn[0], turn[1]};
		int[] newPosition = new int[]{turn[2], turn[3]};
		for (int i = 0; i < queens.length; i++) {
			if (Arrays.equals(queens[i], fromPosition)) {
				newQueens[i] = newPosition;
			} else {
				newQueens[i] = queens[i];
			}
		}
		return newQueens;
	}

	/**
	 * This method calculates the coordinates of all the queens belonging to a specified player
	 *
	 * @param squares the squares array from {@link Board}, empty fields are -1.
	 * @param id      GamePlayerId of the player whose queens are wanted
	 * @return an Array of length nr of queens containing the coordinates {x, y} of the players queens
	 */
	public static int[][] getQueensOfPlayer(int[][] squares, GamePlayerId id) {
		// fill the queens ArrayList with the coordinates of the queens of the player id.
		ArrayList<int[]> queensList = new ArrayList<>();
		for (int x = 0; x < squares.length; x++) {
			for (int y = 0; y < squares[0].length; y++) {
				if (squares[x][y] == id.get()) {
					queensList.add(new int[]{x, y});
				}
			}
		}
		// convert to array as all methods expect an array
		int queen_index = 0;
		int[][] queens = new int[queensList.size()][2];
		for (int[] queen : queensList) {
			queens[queen_index] = queen;
			queen_index++;
		}
		return queens;
	}

	/**
	 * This method evaluates the give board with its applied turn based on the queens' freedom of movement
	 *
	 * @param appliedSquares represents a board with an applied turn,
	 *                       obtained from {@link #cloneAndApply}
	 * @param queens         an Array of length nr of queens containing the coordinates {x, y} of the players queens,
	 *                       obtained from {@link #applyTurnToQueens}
	 * @return the score of the given queens
	 */
	public float evaluateSquares(int[][] appliedSquares, int[][] queens) {
		/**
		 * reachable[0] contains an ArrayList of all positions {x,y} which are reachable in 0 turns, so just the queens
		 * starting position
		 * reachable[x] contains an ArrayList of all positions {x,y} which are reachable in x turns
		 */
		ArrayList<int[]>[] reachable = new ArrayList[this.evaluationDepth + 1];
		for (int i = 0; i < reachable.length; i++) {
			reachable[i] = new ArrayList<>();
		}
		// initialize reachable[0] with the queens' positions
		for (int[] queen : queens) {
			reachable[0].add(queen);
		}
		int curX;
		int curY;
		ArrayList<int[]> foundPositions;
		for (int i = 0; i < this.evaluationDepth; i++) {
			foundPositions = new ArrayList<>();
			for (int[] startPosition : reachable[i]) {
				for (int[] direction : AIPlayerGreedy.directions) {
					curX = startPosition[0] + direction[0];
					curY = startPosition[1] + direction[1];
					while (curX < appliedSquares.length && curX >= 0 && curY < appliedSquares[0].length && curY >= 0) {
						if (appliedSquares[curX][curY] == -2) {
							// field is occupied
							break;
						} else if (appliedSquares[curX][curY] == -1) {
							// field is undiscovered
							foundPositions.add(new int[]{curX, curY});
							appliedSquares[curX][curY] = i + 1;
						}
						curX += direction[0];
						curY += direction[1];
					}
				}
			}
			reachable[i + 1] = foundPositions;
		}
		int weightedSum = 0;
		for (int i = 1; i <= this.evaluationDepth; i++) {
			weightedSum += this.weights[i - 1] * reachable[i].size();
		}
		return weightedSum;
	}

	/**
	 * @param squares     the squares array from {@link Board}, empty fields are -1.
	 * @param turn        int[6] of the form {fromX, fromY, toX, toY, shotX, shotY} representing a possible turn.
	 * @param myQueens    an Array of length nr of queens containing the coordinates {x, y} of the players queens,
	 *                    obtained from {@link #getQueensOfPlayer}
	 * @param otherQueens an Array of length nr of queens containing the coordinates {x, y} of the opponents queens,
	 *                    obtained from {@link #getQueensOfPlayer}
	 * @return the score of the turn
	 */
	public float evaluateTurn(int[][] squares, int[] turn, int[][] myQueens, int[][] otherQueens) {
		// two different board are needed, as evaluateSquares changes the board
		int[][] board1 = cloneAndApply(squares, turn);
		int[][] board2 = cloneAndApply(squares, turn);
		int[][] newQueens = applyTurnToQueens(turn, myQueens);
		float myScore = evaluateSquares(board1, newQueens);
		float otherScore = evaluateSquares(board2, otherQueens);
		float totalScore = ((1 - this.aggressiveness) * myScore) - (this.aggressiveness * otherScore);
		return totalScore;
	}

	/**
	 * This method is the main outward facing method of the AIPlayer.
	 *
	 * @param board the current {@link Board} object
	 * @param id    the GamePlayerId of the player the AI is playing as
	 * @return a {@link Turn} object representing what the AI thinks is its best turn.
	 */
	@Override
	public Turn bestTurn(Board board, GamePlayerId id, Duration maxTurnTime) {
		int[][] squares = board.toSquares();
		int[][] myQueens = getQueensOfPlayer(squares, id);
		int[][] otherQueens = getQueensOfPlayer(squares, id.other());
		ArrayList<int[]> turns = allTurns(squares, myQueens);
		ArrayList<int[]> currentBestTurns = new ArrayList<>();
		float currentBestScore = Float.NEGATIVE_INFINITY;
		if (this.threads < 2) {
			// sequential evaluation
			long abortTime = System.currentTimeMillis() + (maxTurnTime.toMillis() - 1000);
			for (int[] turn : turns) {
				if (System.currentTimeMillis() >= abortTime) {
					System.out.println("!!! MaxTurnTime reached !!!");
					break;
				}
				float score = evaluateTurn(squares, turn, myQueens, otherQueens);
				if (score > currentBestScore) {
					currentBestScore = score;
					currentBestTurns = new ArrayList<>();
					currentBestTurns.add(turn);
				} else if (score == currentBestScore) {
					currentBestTurns.add(turn);
				}
			}
		} else {
			// concurrent evaluation on 'threads' number of threads
			ExecutorService pool = Executors.newFixedThreadPool(this.threads);
			ArrayList<EvaluationTask> tasks = new ArrayList<>();
			EvaluationTask task;
			// create all tasks and enqueue them
			for (int[] turn : turns) {
				task = new EvaluationTask(this, squares, turn, myQueens, otherQueens);
				tasks.add(task);
				// adds task to the queue, is executed when a thread is available
				pool.execute(task);
			}
			// stolen from https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
			pool.shutdown(); // Disable new tasks from being submitted
			try {
				// Wait a while for existing tasks to terminate
				if (!pool.awaitTermination(maxTurnTime.toMillis() - 1000, TimeUnit.MILLISECONDS)) {
					pool.shutdownNow(); // Cancel currently executing tasks
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				pool.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
			// end stolen
			for (EvaluationTask completedTask : tasks) {
				float score = completedTask.getEvaluation();
				if (score > currentBestScore) {
					currentBestScore = score;
					currentBestTurns = new ArrayList<>();
					currentBestTurns.add(completedTask.getTurn());
				} else if (score == currentBestScore) {
					currentBestTurns.add(completedTask.getTurn());
				}
			}
		}
		//System.out.println("My Score: " + currentBestScore);
		// choose a random turn from the best turns
		Random rdm = new Random();
		int rdmIndex = rdm.nextInt(currentBestTurns.toArray().length);
		int[] randomBestTurn = currentBestTurns.get(rdmIndex);
		// construct a turn object
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
	 * This method clones the board.
	 *
	 * @param squares the squares array from {@link Board}, empty fields are -1.
	 * @return an int[][] as the new board. -1 are empty fields and -2 are non-empty fields.
	 */
	public static int[][] cloneSquares(int[][] squares) {
		int[][] result = new int[squares.length][squares[0].length];
		//Copy squares to result, but replay every piececode with -2, because we will write other values on the board later on
		for (int i = 0; i < squares.length; i++) {
			System.arraycopy(squares[i], 0, result[i], 0, squares[i].length);
		}
		return result;
	}

	/**
	 * A class which evaluates a turn, given all the parameters of the {@link AIPlayerGreedy#evaluateTurn} method, and saves the
	 * result.
	 * This is used for the concurrent evaluation of turns using a thread pool.
	 * For a small explanation how we use thread pools visit : https://www.geeksforgeeks.org/thread-pools-java/
	 */
	static class EvaluationTask implements Runnable {

		private final AIPlayerGreedy ai;

		private final int[][] squares;

		private final int[] turn;

		private final int[][] my_queens;

		private final int[][] other_queens;

		private float evaluation = Float.NEGATIVE_INFINITY;

		public EvaluationTask(AIPlayerGreedy ai, int[][] squares, int[] turn, int[][] my_queens, int[][] other_queens) {
			this.ai = ai;
			this.turn = turn;
			this.squares = squares;
			this.my_queens = my_queens;
			this.other_queens = other_queens;
		}

		public void run() {
			this.evaluation = ai.evaluateTurn(squares, turn, my_queens, other_queens);
		}

		public float getEvaluation() {
			return this.evaluation;
		}

		public int[] getTurn() {
			return this.turn;
		}

	}
}