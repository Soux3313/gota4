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
import Player from "../entities/player";
import HTTPError from "../errors/http-error";
import IPlayerService from "../models/services/player.service";
import { TYPES } from "../types";

@controller("/players")
export class PlayersController implements interfaces.Controller {
    @inject(TYPES.PlayerService)
    private players: IPlayerService;

    @httpGet("/")
    private async getAll(@response() res: Response) {
        res.json({
            players: classToPlain(await this.players.getAll()),
        });
    }

    @httpPost("/")
    private async create(@request() req: Request, @response() res: Response) {
        try {
            let player: Player = await transformAndValidate(
                Player,
                req.body as object,
                {
                    transformer: {
                        excludeExtraneousValues: true,
                    },
                }
            );

            player = await this.players.create(
                player,
                (req.body.controllable as boolean) || false
            );

            res.status(200).json(classToPlain(player));
        } catch (error) {
            if (error instanceof HTTPError) {
                res.status(error.statusCode).send(error.message);
            } else if (Array.isArray(error)) {
                // validation issues

                res.status(400).send(error.map((c) => c.toString()).join("\n"));
            } else throw error;
        }
    }

    @httpDelete("/:id")
    private async delete(
        @requestParam("id") id: string,
        @response() res: Response
    ) {
        try {
            await this.players.delete(parseInt(id, 10));
            res.sendStatus(200);
        } catch (error) {
            if (error instanceof HTTPError) {
                res.sendStatus(error.statusCode);
            } else {
                throw error;
            }
        }
    }
}
