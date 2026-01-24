package org.quickstart;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class QuickStartApplication {

    public static void main(String[] args) throws GraphRunnerException {

        ConfigurableApplicationContext applicationContext = SpringApplication.run(QuickStartApplication.class, args);

        ReactAgent weatherAgent = applicationContext.getBean("weatherAgent", ReactAgent.class);


        AssistantMessage assistantMessage = weatherAgent.call("What is the weather like in New York?");


        System.out.println(assistantMessage.getText());

    }
}