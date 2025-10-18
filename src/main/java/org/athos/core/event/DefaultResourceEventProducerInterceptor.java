package org.athos.core.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import one.util.streamex.StreamEx;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.athos.core.scope.context.AthosExecutionContext;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

@Log4j2
public class DefaultResourceEventProducerInterceptor implements ProducerInterceptor<String, Object> {

  private AthosExecutionContext executionContext;
  private ObjectMapper objectMapper;

  @Override
  public void configure(Map<String, ?> configs) {
    this.executionContext = (AthosExecutionContext) configs.get("executionContext");
    this.objectMapper = (ObjectMapper) configs.get("objectMapper");
  }

  @Override
  public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> record) {
    if (CollectionUtils.isEmpty(executionContext.getHeaders())) {
      log.warn("onSend: No headers found in execution context, skipping header propagation for record: {}", record.key());
      return record;
    }
    executionContext.getHeaders().entrySet().stream()
        .flatMap(this::toRecordHeader)
        .forEach(recordHeader -> record.headers().add(recordHeader));
    return record;
  }

  private Stream<RecordHeader> toRecordHeader(Map.Entry<String, Collection<String>> entry) {
    return StreamEx.of(entry.getValue())
        .map(value -> new RecordHeader(entry.getKey(), toBytes(entry.getValue())));
  }

  private byte[] toBytes(Object value) {
    try {
      return objectMapper.writeValueAsBytes(value);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  @Override
  public void onAcknowledgement(RecordMetadata metadata, Exception exception) {}

  @Override
  public void close() {}

}
