package gameserver.result;

/**
 * the type returned by {@link gameserver.Gameserver#removePlayer(model.ids.GlobalPlayerId)}
 * to indicate whether or not the player could be removed
 */
public enum PlayerRemoveResult {
	CANNOT_REMOVE_IN_GAME, // player could not be removed because they where active in a game
	REMOVED                // player was successfully removed
}
