import { injectable } from "inversify";
import GameState from "../entities/game-state";
import GameStatePaderborn from "../entities/game-state-paderborn";
import IGameStateMapper from "../models/mappers/game-state.mapper";

@injectable()
export default class GameStateMapper implements IGameStateMapper {
    toHSA(state: GameStatePaderborn, base?: GameState) {
        if (base === undefined) base = new GameState();

        base.id = state.gameId;
        base.turnPlayer = state.playerId;
        base.winningPlayer = state.winningPlayer;
        if (state.board !== undefined) base.board = state.board;
        if (state.maxTurnTime !== undefined)
            base.maxTurnTime = state.maxTurnTime;
        if (base.turns === undefined) base.turns = [];

        return base;
    }
}
