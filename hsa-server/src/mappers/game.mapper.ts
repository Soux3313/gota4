import { inject, injectable } from "inversify";
import Game from "../entities/game";
import GameCreationDTO from "../entities/game-creation-dto";
import GamePaderborn from "../entities/game-paderborn";
import IGameMapper from "../models/mappers/game.mapper";
import IPlayerMapper from "../models/mappers/player.mapper";
import IPlayerService from "../models/services/player.service";
import { TYPES } from "../types";

@injectable()
export default class GameMapper implements IGameMapper {
    @inject(TYPES.PlayerService)
    private players: IPlayerService;
    @inject(TYPES.PlayerMapper)
    private playerMapper: IPlayerMapper;

    toHSA(game: GamePaderborn) {
        const newGame = new Game();

        newGame.id = game.id;
        newGame.winningPlayer = game.winningPlayer;
        newGame.players = game.players.map((player) =>
            this.players.get(player.id)
        );

        return newGame;
    }

    toPaderborn(game: GameCreationDTO) {
        const newGame = new GamePaderborn();

        newGame.players = game.players.map((id) =>
            this.playerMapper.toPaderborn(this.players.get(id))
        );
        newGame.board = game.board;
        newGame.maxTurnTime = game.maxTurnTime;

        return newGame;
    }
}
