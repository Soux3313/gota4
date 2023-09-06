import { classToPlain } from "class-transformer";
import { transformAndValidate } from "class-transformer-validator";
import { Request, Response } from "express";
import { inject } from "inversify";
import {
    controller,
    httpDelete,
    httpGet,
    httpPost,
    interfaces,
    request,
    requestParam,
    response,
} from "inversify-express-utils";
import GameCreationDTO from "../entities/game-creation-dto";
import HTTPError from "../errors/http-error";
import ValueError from "../errors/value-error";
import IGameService from "../models/services/game.service";
import { TYPES } from "../types";

@controller("/games")
export class GamesController implements interfaces.Controller {
    @inject(TYPES.GameService)
    private games: IGameService;

    @httpGet("/")
    private async getAll(@response() res: Response) {
        try {
            res.json({
                games: classToPlain(await this.games.getAll(), {
                    exposeUnsetFields: false,
                }),
            });
        } catch (error) {
            if (error instanceof HTTPError) {
                res.status(error.statusCode).send(error.message);
            } else {
                throw error;
            }
        }
    }

    @httpGet("/:id")
    private async get(
        @requestParam("id") id: string,
        @response() res: Response
    ) {
        const game = await this.games.get(parseInt(id, 10));

        if (game === undefined) res.status(400).send("game doesn't exist");
        else res.json(classToPlain(game));
    }

    @httpDelete("/:id")
    private async delete(
        @requestParam("id") id: string,
        @response() res: Response
    ) {
        try {
            await this.games.delete(parseInt(id, 10));
            res.sendStatus(200);
        } catch (error) {
            if (error instanceof HTTPError) {
                res.sendStatus(error.statusCode);
            } else {
                throw error;
            }
        }
    }

    @httpPost("/")
    private async create(@request() req: Request, @response() res: Response) {
        try {
            const game: GameCreationDTO = await transformAndValidate(
                GameCreationDTO,
                req.body as object,
                {
                    transformer: {
                        excludeExtraneousValues: true,
                    },
                }
            );

            const pgame = await this.games.create(game);

            res.status(200).json(classToPlain(pgame));
        } catch (error) {
            if (error instanceof HTTPError) {
                res.status(error.statusCode).send(error.message);
            } else if (error instanceof ValueError) {
                res.status(400).send(error.message);
            } else if (Array.isArray(error)) {
                // validation issues

                res.status(400).send(error.map((c) => c.toString()).join("\n"));
            } else throw error;
        }
    }
}
