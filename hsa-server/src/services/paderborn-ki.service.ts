import { inject, injectable } from "inversify";
import fetch, { Response } from "node-fetch";
import { URL } from "url";
import HTTPError from "../errors/http-error";
import IPaderbornKIService from "../models/services/paderborn-ki.service";
import { TYPES } from "../types";

@injectable()
export default class PaderbornKIService implements IPaderbornKIService {
    @inject(TYPES.PaderbornKIPlayerPort)
    private port: string;

    @inject(TYPES.PaderbornKIPlayerURL)
    private url: string;

    private compose(path: string) {
        const url: URL = new URL(path, this.url);

        url.port = this.port;

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

        return await response.text();
    }
}
