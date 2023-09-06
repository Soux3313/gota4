import { Expose } from "class-transformer";
import { ArrayMinSize, IsArray, IsInt } from "class-validator";
import { IsSquare } from "../validators/is-square";

export default class Board {
    @Expose({ name: "gameSizeRows" })
    @IsInt()
    rows: number;

    @Expose({ name: "gameSizeColumns" })
    @IsInt()
    columns: number;

    // validation of multi-dimensional arrays is not yet possible within
    // class-validator
    @Expose()
    @IsArray()
    @ArrayMinSize(1)
    @IsSquare()
    squares: number[][];
}
