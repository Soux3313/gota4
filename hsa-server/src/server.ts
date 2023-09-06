import cors from "cors";
import { Application, json } from "express";
import { Container } from "inversify";
import { getRouteInfo, InversifyExpressServer } from "inversify-express-utils";
import morganBody from "morgan-body";
import * as prettyjson from "prettyjson";

export default function createServer(container: Container): Application {
    const server = new InversifyExpressServer(container);

    server.setConfig((app) => {
        app.use(cors());
        app.use(json());
        morganBody(app, {
            noColors: true,
        });
    });

    const app = server.build();
    const routeInfo = getRouteInfo(container);

    console.log(prettyjson.render({ routes: routeInfo }));

    return app;
}
