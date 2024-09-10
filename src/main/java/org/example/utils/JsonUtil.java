package org.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.example.core.Block;

import java.io.IOException;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static Block parseTransaction(String json) {
        try {
            return objectMapper.readValue(json, Block.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
