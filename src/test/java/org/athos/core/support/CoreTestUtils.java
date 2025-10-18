package org.athos.core.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UtilityClass
public class CoreTestUtils {

  public static void setInternalState(Object target, String field, Object value) {
    try {
      var f = getDeclaredFieldRecursive(field, target.getClass());
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException("Unable to set internal state on a private field", e);
    }
  }

  private static Field getDeclaredFieldRecursive(String field, Class<?> cls) {
    try {
      return cls.getDeclaredField(field);
    } catch (NoSuchFieldException e) {
      if (cls.getSuperclass() != null) {
        return getDeclaredFieldRecursive(field, cls.getSuperclass());
      }
      throw new RuntimeException("Unable to find field: %s for class: %s".formatted(field, cls.getName()), e);
    }
  }

  @SneakyThrows
  public static <T> void assertEqualsDto(T expected, T actual) {
    assertEqualsDto(expected, actual, new ObjectMapper());
  }

  @SneakyThrows
  public static void assertEqualsDto(Object expected, Object actual, ObjectMapper mapper) {
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    actual = actual instanceof String actualStr
        ? mapper.readValue(actualStr, expected.getClass())
        : actual;

    ObjectNode expectedJson = mapper.valueToTree(expected);
    ObjectNode actualJson = mapper.valueToTree(actual);
    removeMetadataRecursively(expectedJson);
    removeMetadataRecursively(actualJson);
    sortArraysRecursively(expectedJson);
    sortArraysRecursively(actualJson);

    String expectedStr = mapper.writeValueAsString(expectedJson);
    String actualStr = mapper.writeValueAsString(actualJson);

    assertEquals(expectedStr, actualStr);
  }

  private static void removeMetadataRecursively(JsonNode node) {
    if (node.isObject()) {
      ObjectNode objNode = (ObjectNode) node;
      objNode.remove("metadata");
      objNode.properties().forEach(entry -> removeMetadataRecursively(entry.getValue()));
    } else if (node.isArray()) {
      node.forEach(CoreTestUtils::removeMetadataRecursively);
    }
  }

  private static void sortArraysRecursively(JsonNode node) {
    if (node.isArray()) {
      ArrayNode arrayNode = (ArrayNode) node;
      List<JsonNode> sorted = StreamSupport.stream(arrayNode.spliterator(), false)
          .sorted(Comparator.comparing(JsonNode::toString))
          .toList();
      arrayNode.removeAll();
      sorted.forEach(arrayNode::add);
      arrayNode.forEach(CoreTestUtils::sortArraysRecursively);
    } else if (node.isObject()) {
      node.properties().forEach(entry -> sortArraysRecursively(entry.getValue()));
    }
  }

}
