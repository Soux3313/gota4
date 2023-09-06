package observer.exceptions;

import org.apache.http.client.methods.HttpGet;

public class FetchGameException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private HttpGet request;
	
	public FetchGameException(HttpGet get) {
		request = get;
	}
	
	public HttpGet getRequest() {
		return request;
	}
}
