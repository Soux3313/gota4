// tslint:disable:ordered-imports

(process.env as any).NODE_TLS_REJECT_UNAUTHORIZED = 0;

import "reflect-metadata";

// everything else follows
import { Container } from "inversify";
import { bindings } from "./inversify.config";
import createServer from "./server";
import { TYPES } from "./types";

(async () => {
    const container = new Container();
    await container.loadAsync(bindings);

    const app = createServer(container);

    app.listen(parseInt(container.get<string>(TYPES.HSAServerPort), 10));
})();
