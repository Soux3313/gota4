import Player from "../../entities/player";
import PlayerPaderborn from "../../entities/player-paderborn";

export default interface IPlayerMapper {
    toPaderborn(player: Player): PlayerPaderborn;
}
