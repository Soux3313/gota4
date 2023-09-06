package model.jsonstruct;

import validation.JsonRequireRecvRecursive;

/**
 * Response delivered to us when performing a GET on /games/<gameId> as specified by the api
 */
public class GetGameResponse {
    @JsonRequireRecvRecursive
    public GameStruct game;
}
