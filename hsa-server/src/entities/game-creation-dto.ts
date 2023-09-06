import { Expose, Type } from "class-transformer";
import {
    ArrayMaxSize,
    ArrayMinSize,
    IsArray,
    IsInt,
    ValidateNested,
} from "class-validator";
import Board from "./board";

// used for validating input for POST /games

export default class GameCreationDTO {
    @Expose()
    @Type(() => Number)
    @IsArray()
    @ArrayMaxSize(2)
    @ArrayMinSize(2)
    @IsInt({
        each: true,
    })
    public players: number[];

    @Expose()
    @Type(() => Board)
    @ValidateNested()
    board: Board;

    @Expose()
    @IsInt()
    maxTurnTime: number;
}
