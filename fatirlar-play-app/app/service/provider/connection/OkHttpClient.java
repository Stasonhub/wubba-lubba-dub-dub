package service.provider.connection;

import javax.inject.Inject;
import javax.inject.Singleton;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

@Singleton
public class OkHttpClient {

    private okhttp3.OkHttpClient okHttpClient;

    @Inject
    public OkHttpClient(ProxyServer proxyServer) throws NoSuchAlgorithmException, KeyManagementException {
        this.okHttpClient = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .proxy(proxyServer.getProxy())
                .sslSocketFactory(sslSocketFactory(), trustManager())
                .hostnameVerifier(hostnameVerifier())
                .build();
    }

    private HostnameVerifier hostnameVerifier() {
        return (hostname, session) -> true;
    }

    private X509TrustManager trustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }
        };
    }

    private SSLSocketFactory sslSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[]{trustManager()}, new java.security.SecureRandom());
        return sslContext.getSocketFactory();
    }

    public okhttp3.OkHttpClient get() {
        return okHttpClient;
    }
}