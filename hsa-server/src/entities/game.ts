import { Expose, Type } from "class-transformer";
import {
    ArrayMaxSize,
    ArrayMinSize,
    IsInt,
    IsOptional,
    ValidateNested,
} from "class-validator";
import Player from "./player";

export default class Game {
    @Expose()
    @IsInt()
    id: number;

    @Expose()
    @Type(() => Player)
    @ArrayMinSize(2)
    @ArrayMaxSize(2)
    @ValidateNested()
    players: Player[];

    @Expose()
    @IsInt()
    @IsOptional()
    winningPlayer: number;
}
