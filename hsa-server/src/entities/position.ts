import { Expose } from "class-transformer";
import { IsInt } from "class-validator";

export default class Position {
    @Expose()
    @IsInt()
    public row: number;

    @Expose()
    @IsInt()
    public column: number;
}
