import Game from "../../entities/game";
import GameCreationDTO from "../../entities/game-creation-dto";
import GamePaderborn from "../../entities/game-paderborn";

export default interface IGameMapper {
    toHSA(game: GamePaderborn): Game;
    toPaderborn(game: GameCreationDTO): GamePaderborn;
}
