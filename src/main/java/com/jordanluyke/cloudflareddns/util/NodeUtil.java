package com.jordanluyke.cloudflareddns.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

public class NodeUtil {
    private static final Logger logger = LogManager.getLogger(NodeUtil.class);

    public static ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static boolean isValidJSON(byte[] json) {
        try {
            return !mapper.readTree(json).isNull();
        } catch(IOException e) {
            return false;
        }
    }

    public static JsonNode getJsonNode(byte[] json) {
        try {
            return mapper.readTree(json);
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static byte[] writeValueAsBytes(Object o) {
        try {
            return mapper.writeValueAsBytes(o);
        } catch(JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> T parseNodeInto(Class<T> clazz, JsonNode body) throws RuntimeException {
        try {
            return mapper.treeToValue(body, clazz);
        } catch(IllegalArgumentException | JsonProcessingException e) {
            logger.error("Json serialize fail: {} {}", clazz, body);
            for(Field field : clazz.getFields()) {
                field.setAccessible(true);
                String name = field.getName();
                if(body.get(name) == null)
                    throw new RuntimeException("Unable to get field: " + name);
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    public static JsonNode parseObjectIntoNode(Object object) {
        try {
            return mapper.valueToTree(object);
        } catch(IllegalArgumentException e) {
            logger.error("Object parse failed: {}", object);
            throw new RuntimeException("Object parse failed");
        }
    }

    public static Optional<JsonNode> get(String field, JsonNode node) {
        return Optional.ofNullable(node.get(field));
    }

    public static Optional<String> getString(String field, JsonNode node) {
        JsonNode fieldNode = node.get(field);
        if(fieldNode == null || fieldNode.isNull())
            return Optional.empty();
        return Optional.of(fieldNode.asText());
    }

    public static Optional<Boolean> getBoolean(String field, JsonNode node) {
        return getString(field, node).map(Boolean::valueOf);
    }

    public static Optional<Integer> getInteger(String field, JsonNode node) {
        return getString(field, node).map(Integer::parseInt);
    }

    public static Optional<BigDecimal> getDecimal(String field, JsonNode node) {
        return getString(field, node).map(BigDecimal::new);
    }

    public static <T> Optional<List<T>> getList(String field, JsonNode node, Class<T[]> clazz) {
        return get(field, node)
                .map(n -> new ArrayList<>(Arrays.asList(mapper.convertValue(n, clazz))));
    }
}
