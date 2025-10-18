package org.athos.core.event.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import one.util.streamex.StreamEx;
import org.athos.core.scope.context.AthosRequestHeaders;
import org.athos.core.scope.AthosExecutionContextSetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public abstract class BaseEventListener {

  @Autowired
  protected ObjectMapper mapper;

  protected <T> T convertValue(Object fromValue, Class<T> toValueType) {
    return fromValue == null ? null : mapper.convertValue(fromValue, toValueType);
  }

  /**
   * Execute the given runnable with RequestContext set from Kafka headers.
   * The context is automatically scoped to the execution and cleaned up after.
   *
   * @param kafkaHeaders Kafka headers from the message
   * @param runnable The logic to execute with the context
   */
  protected void executeWithHeaders(Map<String, Object> kafkaHeaders, Runnable runnable) {
    try (var ignored = new AthosExecutionContextSetter(toContextHeaders(kafkaHeaders))) {
      runnable.run();
    }
  }

  private static Map<String, Collection<String>> toContextHeaders(Map<String, Object> messageHeaders) {
    return StreamEx.of(messageHeaders.entrySet())
        .filter(e -> isValidHeader(e.getKey()))
        .toMap(Map.Entry::getKey, e -> {
          Object x = e.getValue();
          if (x instanceof byte[] bytes) {
            return List.of(new String(bytes, StandardCharsets.UTF_8));
          }
          return List.of(String.valueOf(x));
        });
  }

  private static boolean isValidHeader(String header) {
    return Arrays.stream(AthosRequestHeaders.values()).anyMatch(h -> h.getValue().equals(header));
  }

}

