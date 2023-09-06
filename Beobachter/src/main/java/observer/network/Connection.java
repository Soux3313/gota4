package observer.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Objects;

import observer.exceptions.ConnectionException;

public class Connection {
    private String url;
    private int port;
    private String token;

    public Connection(String url, int port, String token) throws ConnectionException {
        this.url = url;
        this.port = port;
        this.token = token;

        if(!isHostAvailable()) {
            throw new ConnectionException();
        }
    }

    /**
     * Tests if the given host is available by pinging the server
     * @return
     */
    public boolean isHostAvailable() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(url, port), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean equals(Connection connection) {
        return this.url.equals(connection.getUrl()) && this.port == connection.getPort();
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, port);
    }

    @Override
    public String toString() {
        return url + ":" + port;
    }

    public URI toURI() {
        return URI.create("https://"+url+":"+port);
    }

    public String getUrl() {
        return url;
    }

    public int getPort() {
        return port;
    }

    public String getToken() {
    	return token;
    }

	public void setToken(String token) {
		this.token = token;
	}
}
