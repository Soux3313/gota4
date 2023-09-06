package model.jsonstruct;

import validation.JsonRequireRecvRecursive;

/**
 * Response delivered to us when performing a GET on /games/ as specified by the api
 */
public  class GetGamesResponse {
    @JsonRequireRecvRecursive
    public ReducedGameStruct[] games;
}