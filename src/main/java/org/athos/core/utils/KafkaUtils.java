package org.athos.core.utils;

import org.apache.kafka.clients.admin.NewTopic;

import static java.util.Optional.ofNullable;

public class KafkaUtils {

  /**
   * Creates Kafka topic {@link NewTopic} objects for spring context.
   *
   * @param name              - topic name as {@link String}
   * @param numPartitions     - number of partitions as {@link Integer}
   * @param replicationFactor - replication factor as {@link Short}
   * @return created {@link NewTopic} object
   */
  public static NewTopic createTopic(String name, Integer numPartitions, Short replicationFactor) {
    return new NewTopic(name, ofNullable(numPartitions), ofNullable(replicationFactor));
  }

}
