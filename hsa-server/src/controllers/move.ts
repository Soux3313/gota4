import { classToPlain } from "class-transformer";
import { transformAndValidate } from "class-transformer-validator";
import { Request, Response } from "express";
import { inject } from "inversify";
import {
    controller,
    httpPost,
    interfaces,
    request,
    requestParam,
    response,
} from "inversify-express-utils";
import GameStatePaderborn from "../entities/game-state-paderborn";
import Turn from "../entities/turn";
import HTTPError from "../errors/http-error";
import ValueError from "../errors/value-error";
import IGameService from "../models/services/game.service";
import IPlayerService from "../models/services/player.service";
import { TYPES } from "../types";

@controller("/move")
export class MoveController implements interfaces.Controller {
    @inject(TYPES.GameService)
    private games: IGameService;
    @inject(TYPES.PlayerService)
    private players: IPlayerService;

    // the internal endpoint for paderborn communication

    @httpPost("/act")
    private async actOnServer(
        @request() req: Request,
        @response() res: Response
    ) {
        try {
            const gameState = await transformAndValidate(
                GameStatePaderborn,
                req.body as object,
                {
                    transformer: {
                        exposeUnsetFields: false,
                    },
                }
            );

            if (gameState.messageType === "turn")
                req.setTimeout(this.games.get(gameState.gameId).maxTurnTime);

            const response = await this.games.setState(gameState);

            if (response === undefined) res.sendStatus(200);
            else
                res.status(200).json({
                    turn: classToPlain(response),
                });
        } catch (error) {
            if (error instanceof HTTPError) {
                res.status(error.statusCode).send(error.message);
            } else if (Array.isArray(error)) {
                // validation issues

                res.status(400).send(error.map((c) => c.toString()).join("\n"));
            } else throw error;
        }
    }

    @httpPost("/:playerId/:gameId")
    private async sendTurn(
        @requestParam("playerId") playerId: string,
        @requestParam("gameId") gameId: string,
        @request() req: Request,
        @response() res: Response
    ) {
        try {
            const turn = await transformAndValidate(Turn, req.body as object, {
                transformer: {
                    exposeUnsetFields: false,
                },
            });

            // validating that only the current turn player can act
            const game = await this.games.get(parseInt(gameId, 10));

            if (game === undefined) {
                res.status(400).send("invalid game id");
                return;
            }

            const player = this.players.get(parseInt(playerId, 10));

            if (player.id !== game.players[game.turnPlayer].id) {
                res.status(400).send(
                    `player ${player.id} doesn't need to send a turn right now`
                );
                return;
            }

            await this.games.setTurn(parseInt(gameId, 10), turn);

            res.sendStatus(200);
        } catch (error) {
            if (error instanceof ValueError) {
                res.status(400).send(error.message);
            } else if (Array.isArray(error)) {
                // validation issues

                res.status(400).send(error.map((c) => c.toString()).join("\n"));
            } else throw error;
        }
    }
}
