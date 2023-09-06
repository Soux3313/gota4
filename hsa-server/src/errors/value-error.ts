import { CustomError } from "ts-custom-error";

export default class ValueError extends CustomError {
    constructor(message: string) {
        super(message);
    }
}
