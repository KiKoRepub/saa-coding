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

    // 博查AI API 密钥和搜索 URL
    private final String bochaApiKey = "";
    private static final String BOCHA_SEARCH_URL = "https://api.bocha.cn/v1/web-search";

    // Google Search API 的搜索接口地址
    private final String googleApiKey;
    private static final String GOOGLE_SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    public WebSearchTool(String googleApiKey) {
        this.googleApiKey = googleApiKey;

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
                    WebClient webClient = WebClient.builder()
                            .baseUrl(BOCHA_SEARCH_URL)
                            .defaultHeader("Authorization", "Bearer " + bochaApiKey)
                            .defaultHeader("Content-Type", "application/json")
                            .build();


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
    public WebSearchResultDTO googleSearchWeb(
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

    private WebSearchResultDTO executeGoogleSearch(String query, int count) {
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

            return new WebSearchResultDTO(query, result);
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
