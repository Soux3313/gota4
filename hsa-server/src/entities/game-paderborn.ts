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
import PlayerPaderborn from "./player-paderborn";
import Turn from "./turn";

// used for validating data handed to / gotten from the paderborn server

export default class GamePaderborn {
    @Expose({ name: "gameId" })
    @IsInt()
    id: number;

    @Expose()
    @Type(() => PlayerPaderborn)
    @ArrayMinSize(2)
    @ArrayMaxSize(2)
    @ValidateNested()
    players: PlayerPaderborn[];

    @Expose()
    @IsInt()
    @IsOptional()
    winningPlayer: number;

    @Expose()
    @IsOptional()
    @IsArray()
    @ValidateNested()
    @Type(() => Turn)
    turns: Turn[];

    @Expose()
    @IsInt()
    maxTurnTime: number;

    @Expose({ name: "initialBoard" })
    @Type(() => Board)
    @ValidateNested()
    board: Board;
}
