package org.cookpro.dto;
// BochaSearchResponse.java
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@Data
public class BochaSearchResponse {
    private int code;
    private String msg;
    private Data data;

    // getters/setters

    public static class Data {
        @JsonProperty("webPages")
        private WebPages webPages;

        // getter
        public WebPages getWebPages() { return webPages; }
    }

    public static class WebPages {
        private List<Value> value;

        public List<Value> getValue() { return value; }
    }

    public static class Value {
        private String name;
        private String url;
        private String summary;
        @JsonProperty("siteName")
        private String siteName;
        @JsonProperty("siteIcon")
        private String siteIcon;
        @JsonProperty("dateLastCrawled")
        private String dateLastCrawled;

        // getters
        public String getName() { return name; }
        public String getUrl() { return url; }
        public String getSummary() { return summary; }
        public String getSiteName() { return siteName; }
        public String getSiteIcon() { return siteIcon; }
        public String getDateLastCrawled() { return dateLastCrawled; }
    }


}