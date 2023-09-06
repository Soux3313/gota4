import { classToPlain } from "class-transformer";
import { inject, injectable } from "inversify";
import Player from "../entities/player";
import ValueError from "../errors/value-error";
import IPlayerMapper from "../models/mappers/player.mapper";
import IPaderbornServerService from "../models/services/paderborn-server.service";
import IPlayerService from "../models/services/player.service";
import { TYPES } from "../types";

@injectable()
export default class PlayerService implements IPlayerService {
    @inject(TYPES.PaderbornServerService)
    private server: IPaderbornServerService;
    @inject(TYPES.PlayerMapper)
    private playerMapper: IPlayerMapper;

    private nextId: number = 0;
    private players: Map<number, Player> = new Map();

    getAll() {
        return Array.from(this.players.values());
    }

    async create(player: Player, controllable: boolean) {
        player.id = this.nextId++;
        player.controllable = controllable;

        const paderbornPlayer = this.playerMapper.toPaderborn(player);

        const data: any = classToPlain(paderbornPlayer, {
            excludeExtraneousValues: true,
        });

        try {
            await this.server.post("/players/" + player.id.toString(), data);

            this.players.set(player.id, player);

            return player;
        } catch (error) {
            this.nextId--;
            throw error;
        }
    }

    get(id: string | number) {
        if (typeof id === "number") {
            if (this.players.has(id)) return this.players.get(id);
            throw new ValueError(`player with id ${id} not found`);
        } else if (typeof id === "string") {
            for (const player of Array.from(this.players.values())) {
                if (player.name === id) return player;
            }
            throw new ValueError(`player with name ${id} not found`);
        }
    }

    async delete(id: number) {
        await this.server.delete("/players/" + id.toString());

        this.players.delete(id);

        return true;
    }

    async reset() {
        for (const player of this.getAll()) {
            await this.delete(player.id);
        }

        this.nextId = 0;
        this.players.clear();
    }
}
