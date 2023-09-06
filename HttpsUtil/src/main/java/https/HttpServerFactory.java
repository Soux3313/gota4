package https;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

public class HttpServerFactory {
	
	private static String keystorePassword = "securepassword";
	private static String keystoreFilename = "amazonenspiel.keystore";
	
	/**
	 * Creates a new HttpsServer with the ssl part already set up
	 * @param address
	 * @return new https server
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 */
	public static HttpsServer getNewHttpsServer(InetSocketAddress address) throws IOException, UnrecoverableKeyException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException {
		System.setProperty("sun.net.httpserver.maxIdleConnections", "0");

		HttpsServer server = HttpsServer.create(address, 0);
		HttpServerFactory.setupHttps(server);
		
		return server;
	}

	public static HttpServer makeHttpServerOrDie(InetSocketAddress address) {
		try {
			return HttpServer.create(address, 0);
		} catch (Exception e) {
			System.err.println("panic: could not create http server, unable to continue");
			System.exit(1);

			// java compiler cannot figure out that this is unreachable
			// i can't tell it that it's unreachable
			// i can't return null because then on every use the ide complains that the return value may be null
			// so i just have to put some garbage code here
			return (HttpServer) new Object();
		}
	}

	public static HttpsServer makeHttpsServerOrDie(InetSocketAddress address) {
		try {
			return getNewHttpsServer(address);
		} catch (Exception e) {
			System.err.println("panic: could not create https server, unable to continue");
			System.exit(1);

			// java compiler cannot figure out that this is unreachable
			// i can't tell it that it's unreachable
			// i can't return null because then on every use the ide complains that the return value may be null
			// so i just have to put some garbage code here
			return (HttpsServer) new Object();
		}
	}
	
	/**
	 * Configures the ssl-stuff for the HttpsServer
	 * @param server
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 */
	private static void setupHttps(HttpsServer server) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		SSLContext sslContext = SSLContext.getInstance("TLS");

	    // Initialise the keystore
	    char[] password = HttpServerFactory.keystorePassword.toCharArray();
	    KeyStore ks = KeyStore.getInstance("JKS");
	    
	    InputStream keystoreStream = HttpServerFactory.class.getClassLoader().getResourceAsStream(HttpServerFactory.keystoreFilename);
	    
	    if(keystoreStream == null)
	    {	    	
	    	throw new FileNotFoundException("Failed to find keystore file");
	    }
	    
	    ks.load(keystoreStream, password);

	    // Set up the key manager factory
	    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	    kmf.init(ks, password);

	    // Set up the trust manager factory
	    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
	    tmf.init(ks);

	    // Set up the HTTPS context and parameters
	    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	    
	    server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
	        public void configure(HttpsParameters params) {
	            try {
	                // Initialise the SSL context
	                SSLContext c = SSLContext.getDefault();
	                SSLEngine engine = c.createSSLEngine();
	                params.setNeedClientAuth(false);
	                params.setCipherSuites(engine.getEnabledCipherSuites());
	                params.setProtocols(engine.getEnabledProtocols());

	                // Get the default parameters
	                SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
	                params.setSSLParameters(defaultSSLParameters);
	            } catch (Exception e) {	            	
	                e.printStackTrace();
	            }
	        }
	    });
	}
}
