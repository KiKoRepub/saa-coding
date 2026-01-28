package org.cookpro.tools;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;

import org.cookpro.dto.BochaSearchResponse;
import org.cookpro.dto.WebSearchResultDTO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class WebSearchTool {
    private final WebClient webClient;

    // 博查AI API 密钥和搜索 URL
    private final String bochaApiKey = "";
    private static final String BOCHA_SEARCH_URL = "https://api.bocha.cn/v1/web-search";

    // Google Search API 的搜索接口地址
    private final String googleApiKey;
    private static final String GOOGLE_SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    public WebSearchTool(String googleApiKey) {
        this.googleApiKey = googleApiKey;
        this.webClient = WebClient.builder()
                .baseUrl(BOCHA_SEARCH_URL)
                .defaultHeader("Authorization", "Bearer " + bochaApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Tool(name = "boCha_web_search",
            description = "[deprecated] A tool for searching the web for relevant information based on a query.")
    public WebSearchResultDTO boChaSearchWeb(@ToolParam(description = "the question which need to query") String query,
                                             @ToolParam(description = "the number of search results to return,default 5",required = false) int count){

                if (count == 0) {
                    count = 5;
                }
                var requestBody = new SearchRequest(query, "noLimit", true, count);

                try {
                    BochaSearchResponse response = webClient.post()
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(BochaSearchResponse.class)
                            .block();

                    if (response == null || response.getCode() != 200 || response.getData() == null) {
                        return new WebSearchResultDTO(query, "搜索API请求失败，原因: " + (response != null ? response.getMsg() : "未知错误"));
                    }

                    var webPages = response.getData().getWebPages();
                    if (webPages == null || webPages.getValue() == null || webPages.getValue().isEmpty()) {
                        return new WebSearchResultDTO("未找到相关结果。",query);
                    }

                    StringBuilder result = new StringBuilder();
                    for (int i = 0; i < webPages.getValue().size(); i++) {
                        var page = webPages.getValue().get(i);
                        result.append("引用: ").append(i + 1).append("\n")
                                .append("标题: ").append(page.getName()).append("\n")
                                .append("URL: ").append(page.getUrl()).append("\n")
                                .append("摘要: ").append(page.getSummary()).append("\n")
                                .append("网站名称: ").append(page.getSiteName()).append("\n")
                                .append("网站图标: ").append(page.getSiteIcon()).append("\n")
                                .append("发布时间: ").append(page.getDateLastCrawled()).append("\n\n");
                    }

                    return new WebSearchResultDTO(query,result.toString().trim());
                } catch (Exception e) {
                    return new WebSearchResultDTO(query, "搜索API请求失败，原因是：搜索结果解析失败 " + e.getMessage());
                }
    }




    @Tool(description = "Search for information from Search Engine")
    public WebSearchResultDTO googleSearchWeb(
            @ToolParam(description = "Search query keyword") String query,
            @ToolParam(description = "the number of search results to return,default 5",required = false) int count)                                                                                                                                                                                                                                                                                       {
        if (count == 0) {
            count = 5;
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", googleApiKey);
        paramMap.put("engine", SearchEngine.BAIDU.value);
        try {
            String response = HttpUtil.get(GOOGLE_SEARCH_API_URL, paramMap);
            // 取出返回结果的前 count 条
            JSONObject jsonObject = JSONUtil.parseObj(response);
            // 提取 organic_results 部分
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            List<Object> objects = organicResults.subList(0, count);
            // 拼接搜索结果为字符串
            String result = objects.stream().map(obj -> {
                JSONObject tmpJSONObject = (JSONObject) obj;
                return tmpJSONObject.toString();
            }).collect(Collectors.joining(","));

            return new WebSearchResultDTO(query,result);
        } catch (Exception e) {
            return new WebSearchResultDTO(query, "搜索API请求失败，原因是：" + e.getMessage());
        }
    }
    private static class SearchRequest {
        public String query;
        public String freshness;
        public boolean summary;
        public int count;

        public SearchRequest(String query, String freshness, boolean summary, int count) {
            this.query = query;
            this.freshness = freshness;
            this.summary = summary;
            this.count = count;
        }
    }

    private enum SearchEngine {
        GOOGLE("google"),
        BING("bing"),
        BAIDU("baidu")
        ;
        public final String value;
        SearchEngine(String value) {
            this.value = value;
        }

    }

}
