package aiplayer;

import model.board.Board;
import model.ids.GamePlayerId;
import model.player.Turn;

import java.time.Duration;

public interface AIPlayer {
	Turn bestTurn(Board board, GamePlayerId id, Duration maxTurnTime);
}
