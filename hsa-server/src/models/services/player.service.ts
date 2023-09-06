import Player from "../../entities/player";

export default interface IPlayerService {
    create(player: Player, controllable: boolean): Promise<Player>;
    delete(id: number): Promise<boolean>;
    get(id: string | number): Player;
    getAll(): Player[];
    reset(): Promise<void>;
}
