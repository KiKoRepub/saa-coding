package org.cookpro.dto;

import lombok.Data;

@Data
public class WebSearchResultDTO {

    private String question;

    private String result;

    public WebSearchResultDTO(String question, String result) {
        this.question = question;
        this.result = result;
    }
}
