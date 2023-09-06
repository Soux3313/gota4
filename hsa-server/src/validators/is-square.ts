import {
    buildMessage,
    ValidateBy,
    ValidationArguments,
    ValidationOptions,
} from "class-validator";

const IsSquare = (validationOptions?: ValidationOptions) => {
    return ValidateBy(
        {
            name: "isSquare",
            validator: {
                validate: (value: any, args: ValidationArguments) => {
                    if (!Array.isArray(value)) return false;
                    let isSquare = true;
                    for (const row of value) {
                        if (row.length !== value.length) {
                            isSquare = false;
                            break;
                        }
                    }
                    return isSquare;
                },
                defaultMessage: buildMessage(
                    (eachPrefix) =>
                        eachPrefix +
                        `$property needs to be formatted as a 2-dimensional square`
                ),
            },
        },
        validationOptions
    );
};

export { IsSquare };
