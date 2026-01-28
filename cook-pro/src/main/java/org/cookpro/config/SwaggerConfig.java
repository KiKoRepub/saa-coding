package org.cookpro.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Swagger UI: http://localhost:/swagger-ui/index.html
 * OpenAPI JSON: http://localhost:/v3/api-docs
 *
 * HTTP 转换成 MCP 服务
 * https://github.com/sxhxliang/mcp-access-point/blob/main/README_CN.md
 */
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI agentStudyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agent Study API")
                        .description("OpenAPI documentation powered by springdoc-openapi")
                        .version("v1"));
    }


    // 下载 Swagger配置后的接口文档 对应的 OPENAI规范文件
    public static void main(String[] args) throws IOException {
        String basePath = System.getProperty("user.dir");

        String filePath = basePath + "/openAI_doc.json";

        OkHttpClient client = new OkHttpClient();
        String url = "http://localhost:13002/v3/api-docs";

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();

            try (FileWriter fw = new FileWriter(filePath)){
                fw.write(json);
            }
            System.out.println("下载完成 api-docs.json");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
