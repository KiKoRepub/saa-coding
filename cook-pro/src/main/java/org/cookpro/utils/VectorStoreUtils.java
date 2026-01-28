package org.cookpro.utils;

import com.alibaba.cloud.ai.document.JsonDocumentParser;
import org.cookpro.entity.Recipe;
import org.springframework.ai.document.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

public class VectorStoreUtils {


    public static List<Recipe> readJsonFromDoc(String filePath) throws FileNotFoundException {

        JsonDocumentParser jsonDocumentParser = new JsonDocumentParser();

        List<Document> documents =  jsonDocumentParser.parse(new FileInputStream(filePath));
        return Collections.emptyList();
    }



}
