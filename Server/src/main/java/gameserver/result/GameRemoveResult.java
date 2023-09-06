package gameserver.result;

/**
 * the type returned by {@link gameserver.Gameserver#removeGame(model.ids.GameId)}
 * indicating how the game was removed
 */
public enum GameRemoveResult {
	INTERRUPTED_AND_REMOVED, // game was removed but had to be interrupted/aborted to do so
	REMOVED                  // game was removed normally since it was already finished
}
