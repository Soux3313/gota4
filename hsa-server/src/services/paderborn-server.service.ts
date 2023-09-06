import { inject, injectable } from "inversify";
import fetch, { Response } from "node-fetch";
import { URL } from "url";
import HTTPError from "../errors/http-error";
import IPaderbornServerService from "../models/services/paderborn-server.service";
import { TYPES } from "../types";

@injectable()
export default class PaderbornServerService implements IPaderbornServerService {
    @inject(TYPES.PaderbornServerPort)
    private port: string;

    @inject(TYPES.PaderbornServerToken)
    private token: string;

    @inject(TYPES.PaderbornServerURL)
    private url: string;

    async delete(path: string) {
        const response: Response = await fetch(this.compose(path), {
            method: "DELETE",
        });

        if (!response.ok)
            throw new HTTPError(response.status, await response.text());

        return true;
    }

    async get(path: string) {
        const response: Response = await fetch(this.compose(path));

        if (!response.ok)
            throw new HTTPError(response.status, await response.text());

        return response.json();
    }

    private compose(path: string) {
        const url: URL = new URL(path, this.url);

        url.port = this.port;

        url.searchParams.append("token", this.token);

        return url.href;
    }

    async post(path: string, data: object) {
        const response: Response = await fetch(this.compose(path), {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
        });

        if (!response.ok)
            throw new HTTPError(response.status, await response.text());

        return response.text();
    }
}
