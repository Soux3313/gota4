export default interface IPaderbornKIService {
    post(path: string, data: object): Promise<string>;
}
