import { AsyncContainerModule } from "inversify";
import GameStateMapper from "./mappers/game-state.mapper";
import GameMapper from "./mappers/game.mapper";
import PlayerMapper from "./mappers/player.mapper";
import IGameStateMapper from "./models/mappers/game-state.mapper";
import IGameMapper from "./models/mappers/game.mapper";
import IPlayerMapper from "./models/mappers/player.mapper";
import IGameService from "./models/services/game.service";
import IPaderbornKIService from "./models/services/paderborn-ki.service";
import IPaderbornServerService from "./models/services/paderborn-server.service";
import IPlayerService from "./models/services/player.service";
import GameService from "./services/game.service";
import PaderbornKIService from "./services/paderborn-ki.service";
import PaderbornServerService from "./services/paderborn-server.service";
import PlayerService from "./services/player.service";
import { TYPES } from "./types";

export const bindings = new AsyncContainerModule(async (bind) => {
    require("./controllers/games");
    require("./controllers/move");
    require("./controllers/players");
    require("./controllers/reset");

    // these are all configurables
    // could be replaced with environment variables later for more flexibility
    bind<string>(TYPES.HSAServerPort).toConstantValue(
        process.env.HSA_SERVER_PORT ? process.env.HSA_SERVER_PORT : "8060"
    );
    bind<string>(TYPES.HSAServerURL).toConstantValue(
        process.env.HSA_SERVER_URL
            ? process.env.HSA_SERVER_URL
            : "http://localhost"
    );
    bind<string>(TYPES.PaderbornKIPlayerPort).toConstantValue(
        process.env.PADERBORN_KI_PLAYER_PORT
            ? process.env.PADERBORN_KI_PLAYER_PORT
            : "33098"
    );
    bind<string>(TYPES.PaderbornKIPlayerURL).toConstantValue(
        process.env.PADERBORN_KI_PLAYER_URL
            ? process.env.PADERBORN_KI_PLAYER_URL
            : "https://localhost"
    );
    bind<string>(TYPES.PaderbornServerPort).toConstantValue(
        process.env.PADERBORN_SERVER_PORT
            ? process.env.PADERBORN_SERVER_PORT
            : "33100"
    );
    bind<string>(TYPES.PaderbornServerToken).toConstantValue(
        "31415926535897932384626433832795"
    );
    bind<string>(TYPES.PaderbornServerURL).toConstantValue(
        process.env.PADERBORN_SERVER_URL
            ? process.env.PADERBORN_SERVER_URL
            : "https://localhost"
    );

    bind<IGameMapper>(TYPES.GameMapper).to(GameMapper).inSingletonScope();
    bind<IGameStateMapper>(TYPES.GameStateMapper)
        .to(GameStateMapper)
        .inSingletonScope();
    bind<IPlayerMapper>(TYPES.PlayerMapper).to(PlayerMapper).inSingletonScope();

    bind<IGameService>(TYPES.GameService).to(GameService).inSingletonScope();
    bind<IPaderbornKIService>(TYPES.PaderbornKIService)
        .to(PaderbornKIService)
        .inSingletonScope();
    bind<IPaderbornServerService>(TYPES.PaderbornServerService)
        .to(PaderbornServerService)
        .inSingletonScope();
    bind<IPlayerService>(TYPES.PlayerService)
        .to(PlayerService)
        .inSingletonScope();
});
