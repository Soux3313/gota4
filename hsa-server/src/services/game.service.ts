import { classToPlain } from "class-transformer";
import { transformAndValidate } from "class-transformer-validator";
import { validateOrReject } from "class-validator";
import deepcopy from "deepcopy";
import { inject, injectable } from "inversify";
import GameCreationDTO from "../entities/game-creation-dto";
import GamePaderborn from "../entities/game-paderborn";
import GameState from "../entities/game-state";
import GameStatePaderborn from "../entities/game-state-paderborn";
import Turn from "../entities/turn";
import ValueError from "../errors/value-error";
import IGameStateMapper from "../models/mappers/game-state.mapper";
import IGameMapper from "../models/mappers/game.mapper";
import IPlayerMapper from "../models/mappers/player.mapper";
import IGameService from "../models/services/game.service";
import IPaderbornKIService from "../models/services/paderborn-ki.service";
import IPaderbornServerService from "../models/services/paderborn-server.service";
import IPlayerService from "../models/services/player.service";
import { TYPES } from "../types";

interface PendingData {
    rejectionTimer: NodeJS.Timeout;
    rejectFunction: () => void;
    resolveFunction: (turn: Turn) => void;
}

@injectable()
export default class GameService implements IGameService {
    @inject(TYPES.PlayerService)
    private players: IPlayerService;
    @inject(TYPES.PaderbornKIService)
    private kiServer: IPaderbornKIService;
    @inject(TYPES.GameMapper)
    private gameMapper: IGameMapper;
    @inject(TYPES.GameStateMapper)
    private gameStateMapper: IGameStateMapper;
    @inject(TYPES.PlayerMapper)
    private playerMapper: IPlayerMapper;
    @inject(TYPES.PaderbornServerService)
    private server: IPaderbornServerService;

    private nextId: number = 0;
    private pendingResponses: Map<number, PendingData> = new Map();
    private states: Map<number, GameState> = new Map();
    private lastUpdated: Map<number, number> = new Map();

    async getAll() {
        return (
            await transformAndValidate(
                GamePaderborn,
                (
                    await this.server.get("/games")
                ).games as object[]
            )
        ).map((game) => this.gameMapper.toHSA(game));
    }

    get(id: number) {
        if (this.states.has(id)) {
            const state = deepcopy(this.states.get(id));
            const updated = this.lastUpdated.get(id);

            if (
                state.maxTurnTime !== undefined &&
                state.winningPlayer === undefined
            )
                state.remainingTurnTime =
                    state.maxTurnTime - (Date.now() - updated);
            else state.remainingTurnTime = undefined;

            return state;
        }
        return undefined;
    }

    async delete(id: number) {
        if (this.pendingResponses.has(id))
            this.pendingResponses.get(id).rejectFunction();

        this.states.delete(id);
        this.lastUpdated.delete(id);

        await this.server.delete("/games/" + id.toString());

        return true;
    }

    async create(game: GameCreationDTO) {
        const pgame: GamePaderborn = this.gameMapper.toPaderborn(game);

        pgame.id = this.nextId++;

        try {
            await validateOrReject(pgame);
        } catch (error) {
            this.nextId--;

            throw error;
        }

        const data = classToPlain(pgame, {
            exposeUnsetFields: false,
        });

        await this.server.post("/games/" + pgame.id, {
            game: data,
        });

        const hsaGame = this.gameMapper.toHSA(pgame);

        const state = new GameState();

        state.players = hsaGame.players;

        this.states.set(hsaGame.id, state);

        return hsaGame;
    }

    async setState(state: GameStatePaderborn) {
        this.lastUpdated.set(state.gameId, Date.now());

        this.states.set(
            state.gameId,
            this.gameStateMapper.toHSA(state, this.states.get(state.gameId))
        );

        const player = this.states.get(state.gameId).players[state.playerId];

        if (!player.controllable || state.messageType !== "turn") {
            const response = await this.kiServer.post(
                "/",
                classToPlain(state, {
                    exposeUnsetFields: false,
                })
            );

            if (state.messageType === "turn") {
                const turn = await transformAndValidate(
                    Turn,
                    JSON.parse(response).turn as object
                );

                this.states.get(state.gameId).turns.push(turn);

                return turn;
            } else return undefined;
        } else {
            const prom = new Promise<Turn>((resolve, reject) => {
                this.pendingResponses.set(state.gameId, {
                    rejectionTimer: setTimeout(() => {
                        this.pendingResponses.delete(state.gameId);
                        reject();
                    }, this.states.get(state.gameId).maxTurnTime),
                    rejectFunction: () => {
                        clearTimeout(
                            this.pendingResponses.get(state.gameId)
                                .rejectionTimer
                        );
                        this.pendingResponses.delete(state.gameId);
                        reject();
                    },
                    resolveFunction: (turn: Turn) => {
                        clearTimeout(
                            this.pendingResponses.get(state.gameId)
                                .rejectionTimer
                        );
                        this.pendingResponses.delete(state.gameId);
                        this.states.get(state.gameId).turns.push(turn);
                        resolve(turn);
                    },
                });
            });

            return prom;
        }
    }

    async setTurn(game: number, turn: Turn) {
        if (!this.states.has(game))
            throw new ValueError("no game found with that id");

        if (!this.pendingResponses.has(game))
            throw new ValueError(`no pending response for game ${game}`);

        const data = this.pendingResponses.get(game);

        data.resolveFunction(turn);
    }

    async reset() {
        const games = await this.getAll();

        for (const game of games) {
            await this.delete(game.id);
        }

        this.nextId = 0;

        for (const response of Array.from(this.pendingResponses.values())) {
            clearTimeout(response.rejectionTimer);
        }

        this.pendingResponses.clear();
        this.states.clear();
    }
}
