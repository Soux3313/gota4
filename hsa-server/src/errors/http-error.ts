import { CustomError } from "ts-custom-error";

export default class HTTPError extends CustomError {
    constructor(public statusCode: number, message: string) {
        super(message);
    }
}
