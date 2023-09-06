package observer.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;
import java.util.stream.Stream;

import model.board.Board;
import model.board.Piece;
import model.ids.TurnId;
import model.player.Move;
import model.player.Shot;
import model.player.Turn;
import model.util.PieceMap;

public class BoardHistory {

	private final PieceMap map;

    /**
     * Contains all apllied turns
     */
    private final Stack<Turn> turnStack = new Stack<>();

    /**
     * Contains all unapllied turns
     */
    private final LinkedList<Turn> redoList = new LinkedList<>();

    /**
     * Storing all Turns
     */
    private final ArrayList<Turn> allTurns = new ArrayList<>();

    private boolean followLastTurn = true;

    private PropertyChangeSupport support;

    public BoardHistory(PieceMap init) {
        this.support = new PropertyChangeSupport(this);
        this.map = init;
    }

    public void addTurn(Turn turn) {
        //Add Turn to the list for the intermediateSate representation.
        turn.setId(new TurnId(allTurns.size()+1));
        allTurns.add(turn);
        redoList.addLast(turn);
        support.firePropertyChange("TurnAdded", null, turn);
        System.out.println("Listeners: " + Arrays.stream(support.getPropertyChangeListeners()).count());
        System.out.println("Turn added: " + turn.getId().get());
        if(followLastTurn) {
            toLastTurn();
        }
    }

    /**
     * Undo-s the last turn on the intermediateState FieldMap but does not affect the actual FieldMap.
     */
	private void undoLastTurn() {
        if(!turnStack.isEmpty()) {
			followLastTurn = false;

			Turn turn = turnStack.pop();

			Move move = turn.getMove();
			Shot shot = turn.getShot();

			//clear the shot
			map.setAt(shot.getShotPosition(), Piece.Empty);
			//move Amazon to starting position

			Piece tmp = map.getAt(move.getEnd());
			map.setAt(move.getEnd(), Piece.Empty);
			map.setAt(move.getStart(), tmp);

			redoList.addFirst(turn); //same as push on stack
        }
    }

    /**
     * Redo-s the last turn on the intermediateState FieldMap but does not affect the actual FieldMap
     */
	private void doNextTurn() {
        if(!redoList.isEmpty()) {
			followLastTurn = false;

			Turn turn = redoList.getFirst();

			// apply changes to the board:
			Move move = turn.getMove();
			Shot shot = turn.getShot();

			//Get the amazon that is moved:
            map.movePieceTo(move.getStart(), move.getEnd());
            map.setAt(shot.getShotPosition(),Piece.Arrow);

			redoList.removeFirst();
			turnStack.push(turn);
        }
    }

    /**
     * Undo-s all turns on the intermediateState FieldMap but does not affect the actual FieldMap
     */
    public void toFirstTurn() {
        while(!turnStack.isEmpty()) {
            undoLastTurn();
        }
        support.firePropertyChange("TurnApplied", null, null);
    }

    /**
     * Redo-s all turns on the intermediateState FieldMap but does not affect the actual FieldMap
     */
	public void toLastTurn() {
        while(!redoList.isEmpty()) {
            doNextTurn();
        }
        followLastTurn = true;
        support.firePropertyChange("TurnApplied", null, turnStack.peek());
    }

    public void toNextTurn() {
        doNextTurn();
        if(!turnStack.isEmpty()) {
            support.firePropertyChange("TurnApplied", null, turnStack.peek());
        } else {
            support.firePropertyChange("TurnApplied", null, null);
        }
    }

    public void toPreviousTurn() {
        undoLastTurn();
        if(!turnStack.isEmpty()) {
            support.firePropertyChange("TurnApplied", null, turnStack.peek());
        } else {
            support.firePropertyChange("TurnApplied", null, null);
        }
    }

    /**
     * Puts the intermediateState Board to a position where the given turn was just applied
     * @param turn
     */
    public void moveToTurn(Turn turn) {
        if(turnStack.contains(turn)) {
            while(turnStack.peek() != turn) {
                undoLastTurn();
            }
        }
        if(redoList.contains(turn)) {
            while(redoList.getFirst() != turn) {
                doNextTurn();
            }
            doNextTurn();
        }
        followLastTurn = false;
        support.firePropertyChange("TurnApplied", null, turnStack.peek());
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    public boolean hasNextTurn() {
        return !redoList.isEmpty();
    }

    public boolean hasPrevTurn() {
        return !turnStack.isEmpty();
    }

    public Board intoBoard() {
    	return new Board(this.map, this.turnStack.stream().map(Turn::clone));
	}

	public Integer[][] toIntegerSquares() {
    	return this.map.toIntegerSquares();
	}

	public int[][] toSquares() {
    	return this.map.toSquares();
	}

    public Stream<Turn> getAllTurnsAsStream() {
        return allTurns.stream();
    }

    public ArrayList<Turn> getAllTurns() {
        return allTurns;
    }

    public Turn getLastTurn() {
        return this.turnStack.peek();
    }
}
