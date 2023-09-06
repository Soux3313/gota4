package observer.exceptions;

public class UnexpectedServerResponseException extends Exception{
private static final long serialVersionUID = 1L;
	
	public UnexpectedServerResponseException(String resp) {
		super(resp);
	}
}
