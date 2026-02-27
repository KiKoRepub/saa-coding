import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ai.content.Media;
import org.springframework.ai.content.MediaContent;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

public class JsonHandleTest {


    @Test
    public void testOverAllStateConversion() throws Exception {
        // Load the JSON file from resources
        ClassPathResource resource = new ClassPathResource("test-cache.json");
        String json = MessageUtils.readResource(resource);

        // Parse JSON
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> rootMap = mapper.readValue(json, Map.class);

        // Result map
        Map<String, List<AbstractMessage>> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : rootMap.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> overAllState = (Map<String, Object>) entry.getValue();
            Map<String, Object> data = (Map<String, Object>) overAllState.get("OverAllState");
            Map<String, Object> innerData = (Map<String, Object>) data.get("data");
            List<Map<String, Object>> messages = (List<Map<String, Object>>) innerData.get("messages");

            List<AbstractMessage> messageList = new ArrayList<>();
            for (Map<String, Object> msg : messages) {
                String messageType = (String) msg.get("messageType");
                switch (messageType) {
                    case "USER":
                        List<Media> media = (List<Media>) msg.getOrDefault("media", List.of());
                        String text = (String) msg.get("text");
                        Map<String, Object> metadata = (Map<String, Object>) msg.get("metadata");
                        UserMessage userMsg = UserMessage.builder()
                            .text(text)
                            .media(media)
                            .metadata(metadata)
                            .build();
                        messageList.add(userMsg);
                        break;
                    case "ASSISTANT":
                        List<Map<String, Object>> toolCallsRaw = (List<Map<String, Object>>) msg.getOrDefault("toolCalls", List.of());
                        List<AssistantMessage.ToolCall> toolCalls = toolCallsRaw.stream()
                            .map(tc -> new AssistantMessage.ToolCall(
                                (String) tc.get("id"),
                                (String) tc.get("type"),
                                (String) tc.get("name"),
                                (String) tc.get("arguments")
                            ))
                            .toList();
                        Map<String, Object> metadataA = (Map<String, Object>) msg.get("metadata");
                        String textA = (String) msg.get("text");
                        List<Media> mediaA = (List<Media>) msg.getOrDefault("media", List.of());
                        AssistantMessage assistantMsg = AssistantMessage.builder()
                            .content(textA)
                            .properties(metadataA)
                            .toolCalls(toolCalls)
                            .media(mediaA)
                            .build();
                        messageList.add(assistantMsg);
                        break;
                    case "TOOL":
                        List<Map<String, Object>> responsesRaw = (List<Map<String, Object>>) msg.get("responses");
                        List<ToolResponseMessage.ToolResponse> responses = responsesRaw.stream()
                            .map(r -> new ToolResponseMessage.ToolResponse(
                                (String) r.get("id"),
                                (String) r.get("name"),
                                (String) r.get("responseData")
                            ))
                            .toList();
                        Map<String, Object> metadataT = (Map<String, Object>) msg.get("metadata");
                        ToolResponseMessage toolMsg = ToolResponseMessage.builder()
                            .responses(responses)
                            .metadata(metadataT)
                            .build();
                        messageList.add(toolMsg);
                        break;
                }
            }
            result.put(key, messageList);
        }

        // Assertions or logging
        Assert.notNull(result, "Result map should not be null");
        Assert.isTrue(result.size() > 0, "Result map should have entries");
        for (Map.Entry<String, List<AbstractMessage>> entry : result.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Messages: " + entry.getValue().size());
        }
    }

    static final class MessageUtils {

        private MessageUtils() {
        }

        static String readResource(Resource resource) {
            return readResource(resource, Charset.defaultCharset());
        }

        static String readResource(Resource resource, Charset charset) {
            Assert.notNull(resource, "resource cannot be null");
            Assert.notNull(charset, "charset cannot be null");
            try (InputStream inputStream = resource.getInputStream()) {
                return StreamUtils.copyToString(inputStream, charset);
            }
            catch (IOException ex) {
                throw new RuntimeException("Failed to read resource", ex);
            }
        }

    }

}
