package org.deephacks.westty.internal.core.ssl;

import java.io.FileInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

import org.deephacks.tools4j.config.Config;
import org.deephacks.westty.config.SslConfig;

public class WesttySslContextFactory {
    @Config(desc = "Westty SSL Configuration.")
    private SslConfig sslConfig;

    private final String PROTOCOL = "TLS";
    private SSLContext serverContext;
    private SSLContext clientContext;

    public WesttySslContextFactory() {
    }

    public void init() {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }

        SSLContext serverContext = null;
        SSLContext clientContext = null;
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(sslConfig.getKeyStorePath()), sslConfig
                    .getKeyStorePassword().toCharArray());

            // Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, sslConfig.getKeyStorePassword().toCharArray());

            // Initialize the SSLContext to work with our key managers.
            serverContext = SSLContext.getInstance(PROTOCOL);
            serverContext.init(kmf.getKeyManagers(), null, null);
        } catch (Exception e) {
            throw new Error("Failed to initialize the server-side SSLContext", e);
        }
        try {
            clientContext = SSLContext.getInstance(PROTOCOL);
            clientContext.init(null, WesttyTrustManagerFactory.getTrustManagers(), null);
        } catch (Exception e) {
            throw new Error("Failed to initialize the client-side SSLContext", e);
        }

        this.serverContext = serverContext;
        this.clientContext = clientContext;
    }

    public SSLContext getServerContext() {
        return serverContext;
    }

    public SSLContext getClientContext() {
        return clientContext;
    }

    public static class WesttyTrustManagerFactory extends TrustManagerFactorySpi {

        private static final TrustManager DUMMY_TRUST_MANAGER = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                // Always trust - it is an example.
                // You should do something in the real world.
                // You will reach here only if you enabled client certificate auth,
                // as described in SecureChatSslContextFactory.
                System.err.println("UNKNOWN CLIENT CERTIFICATE: " + chain[0].getSubjectDN());
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                // Always trust - it is an example.
                // You should do something in the real world.
                System.err.println("UNKNOWN SERVER CERTIFICATE: " + chain[0].getSubjectDN());
            }
        };

        public static TrustManager[] getTrustManagers() {
            return new TrustManager[] { DUMMY_TRUST_MANAGER };
        }

        @Override
        protected TrustManager[] engineGetTrustManagers() {
            return getTrustManagers();
        }

        @Override
        protected void engineInit(KeyStore keystore) throws KeyStoreException {
            // Unused
        }

        @Override
        protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
                throws InvalidAlgorithmParameterException {
            // Unused
        }
    }
}