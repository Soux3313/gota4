import { Expose } from "class-transformer";
import { IsInt, IsString, ValidateIf } from "class-validator";

export default class PlayerPaderborn {
    @Expose({ name: "playerId" })
    @IsInt()
    id: number;

    @Expose()
    @IsString()
    name: string;

    @Expose()
    @ValidateIf((o) => o.id !== undefined)
    @IsString()
    url: string;
}
