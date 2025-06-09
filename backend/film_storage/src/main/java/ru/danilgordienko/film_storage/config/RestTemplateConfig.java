package ru.danilgordienko.film_storage.config;

//import org.apache.hc.client5.http.auth.AuthScope;
//import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
//import org.apache.hc.client5.http.classic.HttpClient;
//import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
//import org.apache.hc.core5.http.HttpHost;
//import org.apache.hc.core5.util.Timeout;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
//    @Value("${proxy.host}")
//    private String proxyHost;
//
//    @Value("${proxy.port}")
//    private int proxyPort;
//
//    @Value("${proxy.username}")
//    private String proxyUsername;
//
//    @Value("${proxy.password}")
//    private String proxyPassword;
//
//    @Bean
//    public RestTemplate restTemplate() {
//        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
//
//        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
//        credsProvider.setCredentials(
//                new AuthScope(proxyHost, proxyPort),
//                new UsernamePasswordCredentials(proxyUsername, proxyPassword.toCharArray())
//        );
//
//        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
//
//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setDefaultCredentialsProvider(credsProvider)
//                .setRoutePlanner(routePlanner)
//                .setConnectionManagerShared(true)
//                .build();
//
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
//
//        return new RestTemplate(factory);
//    }
}
