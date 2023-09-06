import { inject, injectable } from "inversify";
import Player from "../entities/player";
import PlayerPaderborn from "../entities/player-paderborn";
import IPlayerMapper from "../models/mappers/player.mapper";
import { TYPES } from "../types";

@injectable()
export default class PlayerMapper implements IPlayerMapper {
    @inject(TYPES.HSAServerPort)
    private port: string;
    @inject(TYPES.HSAServerURL)
    private url: string;

    toPaderborn(player: Player) {
        const paderbornPlayer = new PlayerPaderborn();

        paderbornPlayer.id = player.id;
        paderbornPlayer.name = player.name;
        paderbornPlayer.url = this.url + ":" + this.port + "/move/act";

        return paderbornPlayer;
    }
}
