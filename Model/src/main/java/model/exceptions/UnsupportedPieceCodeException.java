package model.exceptions;

/**
 * an exception for when trying to construct a piece from
 * an int that is not a valid piece code specified in the api
 */
public class UnsupportedPieceCodeException extends RuntimeException {

    private final int unsupportedPieceCode;

    public UnsupportedPieceCodeException(String msg, int upc) {
        super(msg);
        this.unsupportedPieceCode = upc;
    }

    public int getUnsupportedPieceCode() {
        return unsupportedPieceCode;
    }
}
