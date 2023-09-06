import { Response } from "express";
import { inject } from "inversify";
import {
    controller,
    httpDelete,
    interfaces,
    response,
} from "inversify-express-utils";
import HTTPError from "../errors/http-error";
import IGameService from "../models/services/game.service";
import IPlayerService from "../models/services/player.service";
import { TYPES } from "../types";

@controller("/reset")
export class ResetController implements interfaces.Controller {
    @inject(TYPES.GameService)
    private games: IGameService;
    @inject(TYPES.PlayerService)
    private players: IPlayerService;

    @httpDelete("/")
    private async resetAll(@response() res: Response) {
        try {
            await this.games.reset();
            await this.players.reset();
            res.sendStatus(200);
        } catch (error) {
            if (error instanceof HTTPError) {
                res.status(error.statusCode).send(error.message);
            } else throw error;
        }
    }
}
