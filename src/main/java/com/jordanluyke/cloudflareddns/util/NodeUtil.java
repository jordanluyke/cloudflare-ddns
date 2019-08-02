package com.jordanluyke.cloudflareddns.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.reactivex.Observable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class NodeUtil {
    private static final Logger logger = LogManager.getLogger(NodeUtil.class);

    public static ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static boolean isValidJSON(byte[] json) {
        try {
            return !mapper.readTree(json).isNull();
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static JsonNode getJsonNode(byte[] json) {
        try {
            return mapper.readTree(json);
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> Observable<T> parseNodeInto(Class<T> clazz, JsonNode body) {
        try {
            return Observable.just(mapper.treeToValue(body, clazz));
        } catch (Exception e) {
            logger.error("Json serialize fail: {}", e.getMessage());
            e.printStackTrace();
            for(Field field : clazz.getFields()) {
                field.setAccessible(true);
                String name = field.getName();
                if(body.get(name) == null)
                    return Observable.error(new RuntimeException("Field required: " + field));
            }
            return Observable.error(new RuntimeException("Parse fail"));
        }
    }

    public static <T> Observable<T> parseNodeInto(Class<T> clazz, Optional<JsonNode> body) {
        return body.map(jsonNode -> parseNodeInto(clazz, jsonNode)).orElseGet(() -> Observable.error(new RuntimeException("Empty body")));
    }

    public static Optional<String> get(String field, JsonNode node) {
        JsonNode fieldNode = node.get(field);
        if(fieldNode == null || fieldNode.isNull())
            return Optional.empty();
        return Optional.of(fieldNode.asText());
    }

    public static String getOrThrow(String field, JsonNode node) {
        return get(field, node).orElseThrow(() -> new RuntimeException("Unable to get: " + field));
    }

    public static Boolean getBoolean(String field, JsonNode node) {
        return get(field, node).map(Boolean::valueOf).orElseThrow(() -> new RuntimeException("Unable to get: " + field));
    }

    public static Integer getInteger(String field, JsonNode node) {
        return get(field, node).map(Integer::parseInt).orElseThrow(() -> new RuntimeException("Unable to get: " + field));
    }
}
