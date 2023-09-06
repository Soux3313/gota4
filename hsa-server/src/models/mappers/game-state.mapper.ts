import GameState from "../../entities/game-state";
import GameStatePaderborn from "../../entities/game-state-paderborn";

export default interface IGameStateMapper {
    toHSA(state: GameStatePaderborn, base?: GameState): GameState;
}
