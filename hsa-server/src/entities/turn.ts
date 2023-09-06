import { Expose, Type } from "class-transformer";
import { ValidateNested } from "class-validator";
import Move from "./move";
import Position from "./position";

export default class Turn {
    @Expose()
    @Type(() => Move)
    @ValidateNested()
    public move: Move;

    @Expose()
    @Type(() => Position)
    @ValidateNested()
    public shot: Position;
}
