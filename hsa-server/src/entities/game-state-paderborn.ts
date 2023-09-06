import { Expose, Type } from "class-transformer";
import { IsInt, IsOptional, IsString, ValidateNested } from "class-validator";
import Board from "./board";
import Turn from "./turn";

export default class GameStatePaderborn {
    @Expose()
    @IsString()
    public messageType: string;

    @Expose()
    @IsInt()
    public gameId: number;

    @Expose()
    @IsInt()
    public playerId: number;

    @Expose()
    @IsInt()
    @IsOptional()
    public turnId: number;

    @Expose()
    @IsInt()
    @IsOptional()
    public winningPlayer: number;

    @Expose()
    @Type(() => Board)
    @ValidateNested()
    public board: Board;

    @Expose()
    @IsInt()
    @IsOptional()
    public maxTurnTime: number;

    @Expose()
    @IsOptional()
    @ValidateNested()
    @Type(() => Turn)
    public enemyTurn: Turn;
}
