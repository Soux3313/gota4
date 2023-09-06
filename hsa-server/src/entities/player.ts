import { Expose } from "class-transformer";
import { IsBoolean, IsInt, IsOptional, IsString } from "class-validator";

export default class Player {
    @Expose()
    @IsOptional()
    @IsInt()
    id: number;

    @Expose()
    @IsString()
    name: string;

    @Expose({
        toPlainOnly: true,
    })
    @IsOptional()
    @IsBoolean()
    controllable: boolean;
}
