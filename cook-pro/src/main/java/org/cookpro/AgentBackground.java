package org.cookpro;


public enum AgentBackground {


    COOKING_ASSISTANT("""
            You are a helpful cooking assistant.
            You can provide recipes, cooking tips, and meal planning advice.
            """),


    ;

    public final String systemPrompt;

     AgentBackground(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}
