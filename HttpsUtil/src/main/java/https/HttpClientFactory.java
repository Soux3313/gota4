package https;

import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

public class HttpClientFactory {


	public static CloseableHttpClient makeHttpsClientOrDie() {
		try {
			return getNewHttpsClient();
		} catch (Exception e) {
			System.err.println("panic: error while creating https client, unable to continue");
			System.exit(1);

			// java compiler cannot figure out that this is unreachable
			// i can't tell it that it's unreachable
			// i can't return null because then on every use the ide complains that the return value may be null
			// so i just have to put some garbage code here
			return (CloseableHttpClient) new Object();
		}
	}

    public static CloseableHttpClient getNewHttpsClient() throws Exception {
      
    	TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, 
          NoopHostnameVerifier.INSTANCE);
        
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
		  .register("https", sslsf)
		  .register("http", new PlainConnectionSocketFactory())
		  .build();

        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf)
          .setConnectionManager(connectionManager).build();
        
        return httpClient;
    }
}