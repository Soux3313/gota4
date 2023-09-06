import Game from "../../entities/game";
import GameCreationDTO from "../../entities/game-creation-dto";
import GameState from "../../entities/game-state";
import GameStatePaderborn from "../../entities/game-state-paderborn";
import Turn from "../../entities/turn";

export default interface IGameService {
    create(game: GameCreationDTO): Promise<Game>;
    delete(id: number): Promise<boolean>;
    get(id: number): GameState | undefined;
    getAll(): Promise<Game[]>;
    reset(): Promise<void>;
    setState(state: GameStatePaderborn): Promise<Turn | undefined>;
    setTurn(game: number, turn: Turn): Promise<void>;
}
