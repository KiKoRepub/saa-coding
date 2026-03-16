package org.cookpro.config.factory;

import io.netty.channel.ChannelOption;
import org.cookpro.config.properties.ToolEnvProperties;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebClientFactory {
    private final String bochaApiKey;
    private final String tavilyApiKey;
    public final String googleApiKey;

    // 使用 Map 管理实例，实现懒加载且线程安全
    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();

    private static final String BOCHA_BASE_URL = "https://api.bocha.cn/v1";
    private static final String GOOGLE_BASE_URL = "https://www.searchapi.io";
    private static final String TAVILY_BASE_URL = "https://api.tavily.com";

    public WebClientFactory(ToolEnvProperties envProperties) {
        this.bochaApiKey = envProperties.getBochaWebSearchApiKey();
        this.tavilyApiKey = envProperties.getTavilyWebSearchApiKey();
        this.googleApiKey = envProperties.getGoogleWebSearchApiKey();
    }

    public WebClient getInstance(String clientType) {
        String type = clientType.toLowerCase();
        return clients.computeIfAbsent(type, this::createWebClient);
    }

    private WebClient createWebClient(String clientType) {
        WebClient.Builder builder = WebClient.builder();

        // 建议配置通用的超时时间
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 连接超时
                .responseTimeout(Duration.ofSeconds(10)); // 响应超时

        builder.clientConnector(new ReactorClientHttpConnector(httpClient));

        switch (clientType) {
            case "google":
                return builder.baseUrl(GOOGLE_BASE_URL).build();
            case "bocha":
                return WebClient.builder().baseUrl(BOCHA_BASE_URL)
                        .defaultHeader("Authorization", "Bearer " + bochaApiKey)
                        .defaultHeader("Content-Type", "application/json")
                        .build();
            case "tavily":
                return builder.baseUrl(TAVILY_BASE_URL)
                        .defaultHeader("Authorization", "Bearer " + tavilyApiKey)
                        .defaultHeader("Content-Type", "application/json")
                        .build();
            default:
                return builder.build();
        }
    }
}