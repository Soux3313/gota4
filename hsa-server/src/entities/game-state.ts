import { Expose, Type } from "class-transformer";
import {
    ArrayMaxSize,
    ArrayMinSize,
    IsArray,
    IsInt,
    IsOptional,
    ValidateNested,
} from "class-validator";
import Board from "./board";
import Player from "./player";
import Turn from "./turn";

export default class GameState {
    @Expose()
    @IsInt()
    id: number;

    @Expose()
    @IsInt()
    turnPlayer: number;

    @Expose()
    @IsInt()
    @IsOptional()
    winningPlayer: number;

    @Expose()
    @Type(() => Board)
    @ValidateNested()
    board: Board;

    @Expose()
    @IsInt()
    @IsOptional()
    maxTurnTime: number;

    @Expose()
    @IsInt()
    @IsOptional()
    remainingTurnTime: number;

    @Expose()
    @IsOptional()
    @IsArray()
    @ValidateNested()
    @Type(() => Turn)
    turns: Turn[];

    @Expose()
    @Type(() => Player)
    @ArrayMinSize(2)
    @ArrayMaxSize(2)
    @ValidateNested()
    players: Player[];
}
