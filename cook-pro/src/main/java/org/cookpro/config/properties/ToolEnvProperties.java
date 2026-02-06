package org.cookpro.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tool-env")
public class ToolEnvProperties {
    private WebSearch webSearch;





    public String getGoogleWebSearchApiKey() {
        return this.webSearch.getGoogle().getApiKey();
    }
    public String getBochaWebSearchApiKey() {
        return this.webSearch.getBocha().getApiKey();
    }

    @Data
    public static class WebSearch {
        private Google google;
        private Bocha bocha;

        @Data
        public static class Google {
            private String apiKey;
        }

        @Data
        public static class Bocha {
            private String apiKey;
        }
    }
}
