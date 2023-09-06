import { Expose, Type } from "class-transformer";
import { ValidateNested } from "class-validator";
import Position from "./position";

export default class Move {
    @Expose()
    @Type(() => Position)
    @ValidateNested()
    public start: Position;

    @Expose()
    @Type(() => Position)
    @ValidateNested()
    public end: Position;
}
