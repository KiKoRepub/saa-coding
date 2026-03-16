package org.cookpro.tools;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;

import org.cookpro.anotations.ProjectTool;
import org.cookpro.config.factory.WebClientFactory;
import org.cookpro.config.properties.ToolEnvProperties;
import org.cookpro.dto.BochaSearchResponse;
import org.cookpro.dto.WebSearchResultDTO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ProjectTool
public class WebSearchTool {
   private final String googleApiKey;
   private final WebClientFactory webClientFactory;

    public WebSearchTool(WebClientFactory factory) {
        this.webClientFactory = factory;
        this.googleApiKey = factory.googleApiKey;
    }

    @Tool(name = "boCha_web_search",
            description = "[deprecated] A tool for searching the web for relevant information based on a query.")
    public WebSearchResultDTO boChaWebSearch(@ToolParam(description = "the question which need to query") String query,
                                             @ToolParam(description = "the number of search results to return,default 5",required = false) int count){

                if (count == 0) {
                    count = 5;
                }
                var requestBody = new SearchRequest(query, "noLimit", true, count);

                try {
                    WebClient webClient = webClientFactory.getInstance("bocha");


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

    @Tool(name = "google_web_search",description = "Search for information from Search Engine")
    public WebSearchResultDTO googleWebSearch(
            @ToolParam(description = "Search query keyword") String query,
            @ToolParam(description = "the number of search results to return,default 5",required = false) int count)                                                                                                                                                                                                                                                                                       {
        if (count == 0) {
            count = 5;
        }

        return new WebSearchResultDTO(query, """
                {
                    "position": 1,
                    "title": "零失败可乐鸡�?!手残党也能秒变大�?,香到舔手指 ",
                    "link ": "https://baijiahao.baidu.com/s?id=1848496521293215736&wfr=spider&for=pc ",
                    "displayed_link ": "天然力的美食频道 ",
                    "snippet ": "如果你实在�?�腥，或者时间紧，焯水也可以：鸡翅冷水下锅，加几片姜和一点料酒，煮开后撇去浮沫，再煮2分钟捞出用温水冲洗干�?。但相信我，试试直接煎，你会打开新世界的大门！步�?3：见证奇迹的时刻—�?��?�入灵魂可乐 当鸡翅两面都煎得金灿灿的时�?�，把姜片和葱段丢进去，�?单翻炒几下，炒出香味。接下来，就�?... ",
                    "snippet_highlighted_words ": [
                      "焯水也可以：鸡翅冷水下锅，加几片姜和�?点料酒，煮开后撇去浮沫，再煮2分钟捞出用温水冲洗干�? "
                    ],
                    "date ": "2025�?11�?11日 ",
                    "thumbnail ": "https://t7.baidu.com/it/u=968932490,1678939288&fm=3035&app=3035&size=re3,2&q=75&n=0&g=4n&f=JPEG&fmt=auto&maxorilen2heic=2000000?s=89B253940A407AC66BA254E50300706A "
                  },
                    "position ": 2,
                  {
                    "title ": "可乐鸡翅怎么做_可乐鸡翅的做法_豆果美食 ",
                    "link ": "https://m.douguo.com/cookbook/2969482.html ",
                    "displayed_link ": "豆果美食 ",
                    "snippet ": "可乐鸡翅的用�? 可乐鸡翅的做�? 步骤1 准备鸡翅�?500�?,翅中现在也的确挺贵的可乐500毫升,远航就不展示�?,做菜之前偷偷喝了几口 步骤2 配件有姜3�?,�?3�?,�?个残缺不全的八角�?�? 步骤3 鸡翅冷水下锅,加姜�?,料酒煮出�?�?,姜片,料酒都是去腥�?,�?锅煮2分钟就可以�?? 步骤4 �?锅后边煮边打�?�?,�?会儿�?... ",
                    "snippet_highlighted_words ": [
                      "可乐鸡翅 "
                    ]
                  }
                """);
//        return executeGoogleSearch(query, count);
    }

    @Tool(name = "tavily_web_search", description = "Search for information from Tavily Search Engine")
    public WebSearchResultDTO tavilyWebSearch(String query, int count) {
            // 1. 初始化 WebClient (建议将 webClient 定义为 Bean 注入)
            WebClient webClient = webClientFactory.getInstance("tavily");

            // 2. 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("search_depth", "smart"); // 可选：basic 或 smart
            requestBody.put("max_results", count);

            // 3. 发起请求并解析结果
            return webClient.post()
                    .uri("/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        // 在这里解析 Tavily 的返回结果并封装进 DTO
                        // Tavily 返回格式通常为: { "results": [ { "title": "...", "content": "...", "url": "..." } ] }
                        String results = response.get("results").toString();

                        return new WebSearchResultDTO(query,results);
                    })
                    .onErrorResume(e -> {
                        // 异常处理：打印日志或返回空结果
                        System.err.println("Tavily API 调用失败: " + e.getMessage());
                        return Mono.just(new WebSearchResultDTO(query, "搜索服务暂时不可用"));
                    })
                    .block(); // 注意：在非响应式方法中必须使用 block() 获取结果
        }

//    private WebSearchResultDTO executeGoogleSearch(String query, int count) {
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("q", query);
//        paramMap.put("api_key", googleApiKey);
//        paramMap.put("engine", SearchEngine.BAIDU.value);
////        /api/v1/search
//    }

    public WebSearchResultDTO searchWeb(String query, int count) {
        // 建议：WebClient 实例应该在类级别初始化或注入
        WebClient webClient = webClientFactory.getInstance("google");

        try {
            // 1. 发起同步 GET 请求
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/search")
                            .queryParam("q", query)
                            .queryParam("api_key", googleApiKey)
                            .queryParam("engine", SearchEngine.BAIDU.value)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // 将异步转为同步

            // 2. 解析逻辑（沿用你现有的 JSONUtil 逻辑）
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");

            if (organicResults == null || organicResults.isEmpty()) {
                return new WebSearchResultDTO(query, "");
            }

            // 3. 截取并拼接结果
            // 增加对 count 范围的保护，防止下标越界
            int limit = Math.min(count, organicResults.size());
            List<Object> subList = organicResults.subList(0, limit);

            String result = subList.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));

            return new WebSearchResultDTO(query, result);

        } catch (Exception e) {
            // 捕获包括网络超时、4xx/5xx 错误、JSON 解析错误在内的所有异常
            return new WebSearchResultDTO(query, "Search API request fails because:" + e.getMessage());
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
