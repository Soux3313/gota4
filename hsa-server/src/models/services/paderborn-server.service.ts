export default interface IPaderbornServerService {
    delete(path: string): Promise<boolean>;
    get(path: string): Promise<any>;
    post(path: string, data: object): Promise<string>;
}
