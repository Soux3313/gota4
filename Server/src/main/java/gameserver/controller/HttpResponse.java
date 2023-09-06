package gameserver.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

/**
 * this class only exists so that we can return it from handlers, that ensures
 * that a handler definitely produces a response because otherwise the compiler will complain
 */
public class HttpResponse {
	private final int code;
	private final String mimeType;
	private final String body;

	/**
	 * ctor
	 *
	 * @param code the http status code of the response
	 * @param mimeType also known as `contentType`
	 * @param body the response body
	 */
	public HttpResponse(int code, String mimeType, String body) {
		this.code = code;
		this.mimeType = mimeType;
		this.body = body;
	}

	public String getBody() {
		return this.body;
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public int getCode() {
		return this.code;
	}

	/**
	 * sends `this` to the exchange partner
	 *
	 * @param exchange the http exchange that generated this message
	 * @throws IOException if the response could not be sent
	 */
	public void respond(HttpExchange exchange) throws IOException {
		Headers h = exchange.getResponseHeaders();
		h.add("Connection", "close");
		h.add("Content-Type", this.mimeType);
		h.add("Access-Control-Allow-Origin","*");

		try (OutputStream out = exchange.getResponseBody()) {
			byte[] b = this.body.getBytes();
			exchange.sendResponseHeaders(code, b.length);
			out.write(b);
		}

		exchange.close();
	}
}
