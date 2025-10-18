package org.athos.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.athos.core.scope.context.AthosExecutionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
@Log4j2
public class AthosAuditorAware implements AuditorAware<UUID> {

  private final AthosExecutionContext athosExecutionContext;

  @Override
  public Optional<UUID> getCurrentAuditor() {
    try {
      return Optional.of(UUID.fromString(athosExecutionContext.getUserId()));
    } catch (Exception e) {
      log.warn("getCurrentAuditor:: Invalid userId format: {} for request id: {}", athosExecutionContext.getUserId(), athosExecutionContext.getRequestId());
      return Optional.empty();
    }
  }

}
