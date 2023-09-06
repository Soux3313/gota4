package aiplayer;

import model.board.Board;
import model.ids.GamePlayerId;
import model.player.Turn;

import java.time.Duration;

public class AIPlayerSwitch implements AIPlayer{

	private final AIPlayerGreedy whitePlayer;
	private final AIPlayerGreedy blackPlayer;

	public AIPlayerSwitch( float[] whiteWeights, float whiteAggressiveness,
						   float[] blackWeights, float blackAggressiveness,
						   int evaluationDepth, int threads) {

		this.whitePlayer = new AIPlayerGreedy(evaluationDepth, whiteWeights, whiteAggressiveness, threads);
		this.blackPlayer = new AIPlayerGreedy(evaluationDepth, blackWeights, blackAggressiveness, threads);
	}

	@Override
	public Turn bestTurn(Board board, GamePlayerId id, Duration maxTurnTime){
		if (id.get() == 0){
			return this.whitePlayer.bestTurn(board, id, maxTurnTime);
		}else{
			return this.blackPlayer.bestTurn(board, id, maxTurnTime);
		}
	}
}
