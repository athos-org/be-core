package org.athos.core.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class KafkaAdminService {

  private final KafkaAdmin kafkaAdmin;
  private final ConfigurableBeanFactory beanFactory;

  /**
   * Registers in spring context {@link NewTopic} bean.
   *
   * <p>It will create kafka topic if not exists.</p>
   *
   * @param newTopic - bean to register
   */
  public void createTopic(NewTopic newTopic) {
    var beanName = newTopic.name() + ".topic";
    if (!beanFactory.containsBean(beanName)) {
      log.info("createTopic:: Creating a Kafka topic: {}", newTopic);
      beanFactory.registerSingleton(beanName, newTopic);
    }
    kafkaAdmin.initialize();
  }

}
