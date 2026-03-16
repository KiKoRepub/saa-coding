package org.cookpro.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tool-env")
public class ToolEnvProperties {
    private WebSearch webSearch;





    public String getGoogleWebSearchApiKey() {
        return this.webSearch.getGoogle().apiKey;
    }
    public String getBochaWebSearchApiKey() {
        return this.webSearch.getBocha().apiKey;
    }

    public String getTavilyWebSearchApiKey() {
        return this.webSearch.getTavily().apiKey;
    }
    @Data
    public static class WebSearch {
        private Google google;
        private Bocha bocha;
        private Tavily tavily;


        public record Google(String apiKey) {
        }

        public record Bocha(String apiKey) {
        }
        public record Tavily(String apiKey) {
        }
    }
}
